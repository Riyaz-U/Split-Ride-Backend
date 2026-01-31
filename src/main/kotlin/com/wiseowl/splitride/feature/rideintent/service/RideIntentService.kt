package com.wiseowl.splitride.feature.rideintent.service

import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.JoinGroupResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideGroupDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideGroupDTO.Companion.toRideGroupDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideGroupMemberDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideIntentResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.toDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.model.RideGroupMember
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus
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
        val startTime = Instant.parse(req.startTime)
        val startTimeHasPassed = startTime.isBefore(Instant.now())
        if(startTimeHasPassed) throw IllegalArgumentException("Invalid start time")

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
            startTime = startTime,
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
                val isActive = it.status == RideIntentStatus.ACTIVE
                val rawDiff = abs(storedAngle - inputAngle)
                val angleDeviation = min(rawDiff, 360 - rawDiff)
                val directionAligned = angleDeviation <= 30

                isActive &&
                        sourceAreaScore >= 0.4f &&
                        destinationAreaScore >= 0.4f &&
                        isSourceWithinBound &&
                        isDestinationWithinBound &&
                        directionAligned &&
                        abs(it.startTime.epochSecond - requestedTime.epochSecond) <= it.flexibleMinutes * 60
            }
    }

    fun getRideGroup(id: UUID): RideGroupDTO{
        val rideGroup = rideGroupRepository.findRideGroupsBy(id) ?: throw IllegalArgumentException("No ride group found with id $id")
        val membersInGroup = rideGroupMemberRepository.findAllByRideGroupId(id)
        return rideGroup.toRideGroupDTO(membersInGroup)
    }

    fun getRideIntent(id: UUID): RideIntentResponseDTO{
        val rideIntent = rideIntentRepository.findById(id).get()
        return rideIntent.toDTO()
    }

    fun getNearbyActiveGroups(latitude: Double, longitude: Double, radiusInMeters: Long): List<RideGroup>{
        val nearbyGroup = rideGroupRepository.findAllByStatusOrderByStartTimeBucketAsc(RideGroupStatus.OPEN)
            .filter {
                geoCalculator.distanceInKm(latitude, longitude, it.sourceLat, it.sourceLng) <= radiusInMeters
            }
        return nearbyGroup
    }

    @Transactional
    fun joinGroup(
        rideIntentId: UUID
    ): JoinGroupResponseDTO {
        val rideIntent = rideIntentRepository.findByIdAndStatus(rideIntentId, RideIntentStatus.ACTIVE)
            ?: throw IllegalArgumentException("RideIntent not found")

        val availableGroup = rideGroupRepository.findByDirectionAndStartTimeBucket(
            rideIntent.direction,
            timerBucket.get(rideIntent.startTime)
        ).firstOrNull {
            val isSourceWithinBound = geoCalculator.distanceInKm(rideIntent.sourceLat, rideIntent.sourceLng, it.sourceLat, it.sourceLng) < MATCH_BOUND_DISTANCE_KM
            val isDestinationWithinBound = geoCalculator.distanceInKm(rideIntent.destinationLat, rideIntent.destinationLng, it.destinationLat, it.destinationLng) < MATCH_BOUND_DISTANCE_KM
            val isOpen = it.status == RideGroupStatus.OPEN
            isSourceWithinBound && isDestinationWithinBound && isOpen
        }
        val updatedGroup: RideGroup = availableGroup ?: RideGroup(
            direction = rideIntent.direction,
            sourceLat = rideIntent.sourceLat,
            sourceLng = rideIntent.sourceLng,
            destinationLat = rideIntent.destinationLat,
            destinationLng = rideIntent.destinationLng,
            startTimeBucket = timerBucket.get(rideIntent.startTime)
        )

        val createdRideGroup = rideGroupRepository.save(updatedGroup)

        val rideGroupMember = rideGroupMemberRepository
            .findByRideGroupIdAndRideIntentId(
                createdRideGroup.id!!
                , rideIntent.id!!
            )
        val alreadyJoined = rideGroupMember!=null

        if (!alreadyJoined) {
            val newMember = RideGroupMember(rideGroupId = createdRideGroup.id, rideIntentId = rideIntent.id)
            rideGroupMemberRepository.save(newMember)
            rideIntentRepository.save(rideIntent.copy(status = RideIntentStatus.GROUPED))
        }

        val allMemberForGroup = rideGroupMemberRepository.findAllByRideGroupId(createdRideGroup.id)
        val isGroupFull = rideGroupMemberRepository.countByRideGroupId(createdRideGroup.id) >= updatedGroup.maxSize
        if(isGroupFull){
            rideGroupRepository.save(
                createdRideGroup.copy(
                    status = RideGroupStatus.FULL
                )
            )
        }

        return JoinGroupResponseDTO(
            rideGroupId = createdRideGroup.id,
            currentMembers = allMemberForGroup.map { RideGroupMemberDTO(it.id, it.rideIntentId, it.joinedAt) },
            isGroupFull = isGroupFull
        )
    }

    @Transactional
    fun cancelRideIntent(rideIntentId: UUID, userId: UUID): Boolean{
        val rideIntentToCancel = rideIntentRepository.findByIdAndUserId(rideIntentId, userId)
            ?: throw IllegalArgumentException("RideIntent not found")

        when(rideIntentToCancel.status){
            RideIntentStatus.ACTIVE -> {
                rideIntentRepository.save(rideIntentToCancel.copy(status = RideIntentStatus.CANCELLED))
                return true
            }
            RideIntentStatus.GROUPED -> {
                val deletedMember = rideGroupMemberRepository.deleteByRideIntentId(rideIntentId) //Exit Ride Group by deleting Ride Group Member
                val group = rideGroupRepository.findRideGroupsBy(deletedMember.rideGroupId) //Exit Ride Group by deleting Ride Group Member
                val numberOfMembersLeftInTheGroup = rideGroupMemberRepository.countByRideGroupId(deletedMember.rideGroupId)
                rideGroupRepository.save(
                    when (numberOfMembersLeftInTheGroup) {
                        0 -> group.copy(status = RideGroupStatus.CANCELLED)
                        group.maxSize -> group.copy(status = RideGroupStatus.FULL)
                        else -> group.copy(status = RideGroupStatus.OPEN)
                    }
                )
                if(numberOfMembersLeftInTheGroup < 2){
                    //Cancel group
                    rideGroupRepository.deleteById(deletedMember.rideGroupId)
                } else rideGroupRepository.save(group.copy(status = RideGroupStatus.OPEN))
                rideIntentRepository.save(rideIntentToCancel.copy(status = RideIntentStatus.CANCELLED))
                return true
            }
            RideIntentStatus.CANCELLED -> throw IllegalArgumentException("RideIntent has already been cancelled")
            RideIntentStatus.COMPLETED -> throw IllegalArgumentException("RideIntent has already been completed")
            RideIntentStatus.EXPIRED -> throw IllegalArgumentException("RideIntent has been expired")
        }
    }

    @Transactional
    fun getGroupsByUser(userId: UUID): List<RideGroup> {
        val rideGroups =
            rideIntentRepository.findAllByUserId(userId)
                .map {
                    rideGroupMemberRepository.findByRideIntentId(it.id!!)
                }.map {
                    rideGroupRepository.findRideGroupsBy(it!!.rideGroupId)
                }

        return rideGroups
    }
}
