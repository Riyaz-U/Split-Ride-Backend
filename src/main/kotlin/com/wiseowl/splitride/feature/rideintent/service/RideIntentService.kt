package com.wiseowl.splitride.feature.rideintent.service

import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideGroupMemberDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.model.RideGroupMember
import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupMemberRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
import com.wiseowl.splitride.feature.rideintent.util.AreaNormalizer
import com.wiseowl.splitride.feature.rideintent.util.GeoCalculator
import com.wiseowl.splitride.feature.rideintent.util.KeywordExtractor
import com.wiseowl.splitride.feature.rideintent.util.KeywordMatcher
import com.wiseowl.splitride.feature.rideintent.util.TimerBucket
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.math.abs
import kotlin.math.min

private const val MATCH_BOUND_DISTANCE_KM = 0.5

@Service
class RideIntentService(
    private val rideIntentRepository: RideIntentRepository,
    private val rideGroupRepository: RideGroupRepository,
    private val rideGroupMemberRepository: RideGroupMemberRepository,
    private val areaNormalizer: AreaNormalizer,
    private val keywordExtractor: KeywordExtractor,
    private val keywordMatcher: KeywordMatcher,
    private val geoCalculator: GeoCalculator,
    private val timerBucket: TimerBucket,
) {

    fun create(req: CreateRideIntentRequestDTO): RideIntent {
        val normalizedSource = areaNormalizer.normalize(req.sourceArea)
        val normalizedDestination = areaNormalizer.normalize(req.destinationArea)
        val sourceKeyword = keywordExtractor.extractKeywords(normalizedSource).joinToString(",")
        val destinationKeyword = keywordExtractor.extractKeywords(normalizedDestination).joinToString(",")
        val intent = RideIntent(
            userId = UUID.fromString(req.userId),
            direction = req.direction,
            sourceArea = req.sourceArea,
            destinationArea = req.destinationArea,
            normalizedSource = normalizedSource,
            normalizedDestination = normalizedDestination,
            sourceLat = req.sourceLat,
            sourceLng = req.sourceLng,
            destinationLat = req.destinationLat,
            destinationLng = req.destinationLng,
            sourceKeywords = sourceKeyword,
            destinationKeywords = destinationKeyword,
            startTime = Instant.parse(req.startTime),
            flexibleMinutes = req.flexibleMinutes
        )
        return rideIntentRepository.save(intent)
    }

    fun search(
        direction: Direction,
        sourceArea: String,
        destinationArea: String,
        sourceLat: Double,
        sourceLng: Double,
        destinationLat: Double,
        destinationLng: Double,
        time: String
    ): List<RideIntent> {
        val normSource = areaNormalizer.normalize(sourceArea)
        val normDest = areaNormalizer.normalize(destinationArea)
        val requestedTime = Instant.parse(time)

        return rideIntentRepository.findAllByDirection(direction)
            .filter {
                val sourceAreaScore = keywordMatcher.getScore(normSource, it.sourceKeywords.split(",").toSet())
                val destinationAreaScore = keywordMatcher.getScore(normDest, it.destinationKeywords.split(",").toSet())
                val isSourceWithinBound = geoCalculator.distanceInKm(sourceLat, sourceLng, it.sourceLat, it.sourceLng) < MATCH_BOUND_DISTANCE_KM
                val isDestinationWithinBound = geoCalculator.distanceInKm(destinationLat, destinationLng, it.destinationLat, it.destinationLng) < MATCH_BOUND_DISTANCE_KM
                val storedAngle = geoCalculator.angle(it.sourceLat, it.sourceLng, it.destinationLat, it.destinationLng)
                val inputAngle = geoCalculator.angle(sourceLat, sourceLng, destinationLat, destinationLng)
                val rawDiff = abs(storedAngle - inputAngle)
                val angleDeviation = min(rawDiff, 360 - rawDiff)
                val directionAligned = angleDeviation <= 30

                it.status == RideIntentStatus.ACTIVE &&
                        sourceAreaScore >= 0.4f &&
                        destinationAreaScore >= 0.4f &&
                        isSourceWithinBound &&
                        isDestinationWithinBound &&
                        directionAligned &&
                        abs(it.startTime.epochSecond - requestedTime.epochSecond) <= it.flexibleMinutes * 60
            }
    }

    fun getGroup(id: UUID): RideGroup{
        return rideGroupRepository.findById(id).get()
    }

    @Transactional
    fun joinGroup(
        rideIntentId: UUID
    ): JoinGroupResponseDTO {
        val rideIntent = rideIntentRepository.findByIdOrNull(rideIntentId)
            ?: throw IllegalArgumentException("RideIntent not found")

        val availableGroup = rideGroupRepository.findByDirectionAndStartTimeBucket(
            rideIntent.direction,
            timerBucket.get(rideIntent.startTime)
        ).firstOrNull {
            val isSourceWithinBound = geoCalculator.distanceInKm(rideIntent.sourceLat, rideIntent.sourceLng, it.sourceLat, it.sourceLng) < MATCH_BOUND_DISTANCE_KM
            val isDestinationWithinBound = geoCalculator.distanceInKm(rideIntent.destinationLat, rideIntent.destinationLng, it.destinationLat, it.destinationLng) < MATCH_BOUND_DISTANCE_KM
            val occupancy = rideGroupMemberRepository.findAllByRideGroupId(it.id!!).size
            val isSpaceAvailable = occupancy < it.maxSize
            isSourceWithinBound && isDestinationWithinBound && isSpaceAvailable
        }
        val updatedGroup = availableGroup ?: RideGroup(
            direction = rideIntent.direction,
            sourceLat = rideIntent.sourceLat,
            sourceLng = rideIntent.sourceLng,
            destinationLat = rideIntent.destinationLat,
            destinationLng = rideIntent.destinationLng,
            startTimeBucket = timerBucket.get(rideIntent.startTime)
        )
        rideGroupRepository.save(updatedGroup)

        val newMember = RideGroupMember(rideGroupId = updatedGroup.id!!, rideIntentId = rideIntent.id!!)
        rideGroupMemberRepository.save(newMember)
        val allMemberForGroup = rideGroupMemberRepository.findAllByRideGroupId(updatedGroup.id)

        val isGroupFull = rideGroupMemberRepository.findAllByRideGroupId(updatedGroup.id).size >= updatedGroup.maxSize
        return JoinGroupResponseDTO(
            rideGroupId = updatedGroup.id,
            currentMembers = allMemberForGroup.map { RideGroupMemberDTO(it.id, it.rideIntentId, it.joinedAt) },
            isGroupFull = isGroupFull
        )
    }
}
