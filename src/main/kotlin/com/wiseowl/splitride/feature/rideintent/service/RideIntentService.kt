package com.wiseowl.splitride.feature.rideintent.service

import com.wiseowl.splitride.feature.rideintent.dto.CreateRideIntentRequestDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideGroupDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideGroupDTO.Companion.toRideGroupDTO
import com.wiseowl.splitride.feature.rideintent.dto.RideIntentResponseDTO
import com.wiseowl.splitride.feature.rideintent.dto.toDTO
import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus
import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.model.RideSearchProcess
import com.wiseowl.splitride.feature.rideintent.model.RideSearchProcessState
import com.wiseowl.splitride.feature.rideintent.model.ScheduleType
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupMemberRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideSearchProcessRepository
import com.wiseowl.splitride.feature.rideintent.util.GeoCalculator
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

private const val MATCH_BOUND_DISTANCE_KM = 0.5

@Service
class RideIntentService(
    private val rideIntentRepository: RideIntentRepository,
    private val rideGroupRepository: RideGroupRepository,
    private val rideGroupMemberRepository: RideGroupMemberRepository,
    private val rideSearchProcessRepository: RideSearchProcessRepository,
    private val geoCalculator: GeoCalculator,
) {
    fun createIntent(
        userId: String,
        req: CreateRideIntentRequestDTO
    ): RideIntent {
        if(req.scheduleType is ScheduleType.Future){
            val startTime = Instant.parse(req.scheduleType.startTime)
            val startTimeHasPassed = startTime.isBefore(Instant.now())
            if(startTimeHasPassed) throw IllegalArgumentException("Invalid start time")
        }

        val intent = RideIntent(
            userId = UUID.fromString(userId),
            sourceLat = req.sourceLat,
            sourceLng = req.sourceLng,
            destinationLat = req.destinationLat,
            destinationLng = req.destinationLng,
            scheduleType = req.scheduleType,
            flexibleMinutes = req.flexibleMinutes
        )

        return rideIntentRepository.save(intent)
    }

    fun scheduleSearch(
        rideIntentId: UUID
    ): UUID {
        val searchProcess = RideSearchProcess(rideIntentId = rideIntentId)
        val searchProcessId = rideSearchProcessRepository.save(searchProcess).id!!
        return searchProcessId
    }

    fun searchStatus(
        rideSearchId: UUID
    ): RideSearchProcessState {
        val searchProcess = rideSearchProcessRepository.findById(rideSearchId).getOrElse { throw Exception("Unable to find search process") }
        return searchProcess.status
    }

    fun getRideGroup(id: UUID): RideGroupDTO{
        val rideGroup = rideGroupRepository.findById(id).getOrNull() ?: throw IllegalArgumentException("No ride group found with id $id")
        val membersInGroup = rideGroupMemberRepository.findAllByRideGroupId(id)
        return rideGroup.toRideGroupDTO(membersInGroup)
    }

    fun getRideIntent(id: UUID): RideIntentResponseDTO{
        val rideIntent = rideIntentRepository.findById(id).get()
        return rideIntent.toDTO()
    }

    fun getNearbyActiveGroups(latitude: Double, longitude: Double, radiusInMeters: Long): List<RideGroup>{
        val nearbyGroup = rideGroupRepository.findAllByStatus(RideGroupStatus.OPEN)
            .filter {
                geoCalculator.distanceInKm(latitude, longitude, it.sourceLat, it.sourceLng) <= radiusInMeters
            }
        return nearbyGroup
    }

//    @Transactional
//    fun joinGroup(
//        rideGroupId: UUID
//    ): JoinGroupResponseDTO {
//        val rideIntent = rideIntentRepository.findByIdAndStatus(rideIntentId, RideIntentStatus.ACTIVE)
//            ?: throw IllegalArgumentException("RideIntent not found")
//
//        val availableGroup = rideGroupRepository.findByDirectionAndStartTimeBucket(
//            rideIntent.direction,
//            timerBucket.get(rideIntent.startTime)
//        ).firstOrNull {
//            val isSourceWithinBound = geoCalculator.distanceInKm(rideIntent.sourceLat, rideIntent.sourceLng, it.sourceLat, it.sourceLng) < MATCH_BOUND_DISTANCE_KM
//            val isDestinationWithinBound = geoCalculator.distanceInKm(rideIntent.destinationLat, rideIntent.destinationLng, it.destinationLat, it.destinationLng) < MATCH_BOUND_DISTANCE_KM
//            val isOpen = it.status == RideGroupStatus.OPEN
//            isSourceWithinBound && isDestinationWithinBound && isOpen
//        }
//        val updatedGroup: RideGroup = availableGroup ?: RideGroup(
//            direction = rideIntent.direction,
//            sourceLat = rideIntent.sourceLat,
//            sourceLng = rideIntent.sourceLng,
//            destinationLat = rideIntent.destinationLat,
//            destinationLng = rideIntent.destinationLng,
//            startTimeBucket = timerBucket.get(rideIntent.startTime)
//        )
//
//        val createdRideGroup = rideGroupRepository.save(updatedGroup)
//
//        val rideGroupMember = rideGroupMemberRepository
//            .findByRideGroupIdAndRideIntentId(
//                createdRideGroup.id!!
//                , rideIntent.id!!
//            )
//        val alreadyJoined = rideGroupMember!=null
//
//        if (!alreadyJoined) {
//            val newMember = RideGroupMember(rideGroupId = createdRideGroup.id, rideIntentId = rideIntent.id)
//            rideGroupMemberRepository.save(newMember)
//            rideIntentRepository.save(rideIntent.copy(status = RideIntentStatus.GROUPED))
//        }
//
//        val allMemberForGroup = rideGroupMemberRepository.findAllByRideGroupId(createdRideGroup.id)
//        val isGroupFull = rideGroupMemberRepository.countByRideGroupId(createdRideGroup.id) >= updatedGroup.maxSize
//        if(isGroupFull){
//            rideGroupRepository.save(
//                createdRideGroup.copy(
//                    status = RideGroupStatus.FULL
//                )
//            )
//        }
//
//        return JoinGroupResponseDTO(
//            rideGroupId = createdRideGroup.id,
//            currentMembers = allMemberForGroup.map { RideGroupMemberDTO(it.id, it.rideIntentId, it.joinedAt) },
//            isGroupFull = isGroupFull
//        )
//    }

    @Transactional
    fun cancelRideIntent(rideIntentId: UUID, userId: String): Boolean{
        val rideIntentToCancel = rideIntentRepository.findByIdAndUserId(rideIntentId, UUID.fromString(userId))
            ?: throw IllegalArgumentException("RideIntent not found")

        when(rideIntentToCancel.status){
            RideIntentStatus.ACTIVE -> {
                rideIntentRepository.save(rideIntentToCancel.copy(status = RideIntentStatus.CANCELLED))
                rideGroupMemberRepository.deleteByRideIntentId(rideIntentId)
                return true
            }
            RideIntentStatus.GROUPED -> {
                val deletedMember = rideGroupMemberRepository.deleteByRideIntentId(rideIntentId) //Exit Ride Group by deleting Ride Group Member
                val group = rideGroupRepository.findById(deletedMember.rideGroupId).get() //Exit Ride Group by deleting Ride Group Member
                val numberOfMembersLeftInTheGroup = rideGroupMemberRepository.countByRideGroupId(deletedMember.rideGroupId)
                if (numberOfMembersLeftInTheGroup<1) rideGroupRepository.save(group.copy(status = RideGroupStatus.CANCELLED))
                else rideGroupRepository.save(group.copy(status = RideGroupStatus.OPEN))
                return true
            }
            RideIntentStatus.CANCELLED -> throw IllegalArgumentException("RideIntent has already been cancelled")
            RideIntentStatus.COMPLETED -> throw IllegalArgumentException("RideIntent has already been completed")
            RideIntentStatus.EXPIRED -> throw IllegalArgumentException("RideIntent has been expired")
        }
    }

    @Transactional
    fun getGroupsByUser(userId: String): List<RideGroup> {
        val rideGroups =
            rideIntentRepository.findAllByUserId(UUID.fromString(userId))
                .map {
                    rideGroupMemberRepository.findByRideIntentId(it.id!!)
                }.map {
                    rideGroupRepository.findById(it!!.rideGroupId).get()
                }

        return rideGroups
    }
}
