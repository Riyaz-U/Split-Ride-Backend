package com.wiseowl.splitride.feature.rideintent.service

import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupResponseDTO
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
    private val geoCalculator: GeoCalculator
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

    fun joinGroup(
        req: JoinGroupRequestDTO
    ): JoinGroupResponseDTO {
        val availableGroup = rideGroupRepository.findAll().first {
            val isSourceWithinBound = geoCalculator.distanceInKm(req.sourceLat, req.sourceLng, it.sourceLat, it.sourceLng) < MATCH_BOUND_DISTANCE_KM
            val isDestinationWithinBound = geoCalculator.distanceInKm(req.destinationLat, req.destinationLng, it.destinationLat, it.destinationLng) < MATCH_BOUND_DISTANCE_KM
            val isSpaceAvailable = it.occupancy < it.maxSize
            val isStartTimeWithinBound = with(Instant.parse(req.time)){
                it.startTimeBucket.epochSecond > epochSecond
                        && abs(it.startTimeBucket.epochSecond - epochSecond) < 10 * 60
            }
            isSourceWithinBound && isDestinationWithinBound && isStartTimeWithinBound && isSpaceAvailable
        }
        val updatedGroup = availableGroup?.copy(occupancy = availableGroup.occupancy+1) ?: RideGroup(
            direction = req.direction,
            sourceLat = req.sourceLat,
            sourceLng = req.sourceLng,
            destinationLat = req.destinationLat,
            destinationLng = req.destinationLng,
            startTimeBucket = Instant.parse(req.time),
            occupancy = 1
        )
        rideGroupRepository.save(updatedGroup)

        val newMember = RideGroupMember(rideGroupId = updatedGroup.id!!, rideIntentId = req.rideIntentId)
        rideGroupMemberRepository.save(newMember)
        val allMemberForGroup = rideGroupMemberRepository.findAllByRideGroupId(updatedGroup.id)

        return JoinGroupResponseDTO(
            rideGroupId = updatedGroup.id,
            currentMembers = allMemberForGroup,
            isGroupFull = updatedGroup.occupancy >= updatedGroup.maxSize
        )
    }
}
