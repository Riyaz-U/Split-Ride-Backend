package com.wiseowl.splitride.job

import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.model.RideGroupMember
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus
import com.wiseowl.splitride.feature.rideintent.model.RideSearchProcessState
import com.wiseowl.splitride.feature.rideintent.model.ScheduleType
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupMemberRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideSearchProcessRepository
import com.wiseowl.splitride.feature.rideintent.util.GeoCalculator
import com.wiseowl.splitride.feature.rideintent.util.TimerBucket
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

private const val MATCH_BOUND_DISTANCE_KM = 0.5

@Service
class RideSearchProcessingJob(
    private val rideIntentRepository: RideIntentRepository,
    private val rideGroupRepository: RideGroupRepository,
    private val rideGroupMemberRepository: RideGroupMemberRepository,
    private val rideSearchProcessRepository: RideSearchProcessRepository,
    private val geoCalculator: GeoCalculator,
    private val taskScheduler: TaskScheduler
) {
    @Scheduled(cron = "0 * * * * *")
    fun scheduleSearch() {
        val idleRideSearchProcesses = rideSearchProcessRepository.findAllByStatus(RideSearchProcessState.Idle)
        idleRideSearchProcesses.forEach { process ->
            val rideIntent = rideIntentRepository.findById(process.id).getOrNull()
            if(rideIntent == null) {
                //TODO("Log it! Should not happen")
                rideSearchProcessRepository.delete(process)
            } else {
                rideSearchProcessRepository.save(process.copy(status = RideSearchProcessState.Searching))
                taskScheduler.scheduleAtFixedRate(
                    {
                        val openGroups = rideGroupRepository.findAllByStatus(RideGroupStatus.OPEN)
                        val foundMatchingGroup = openGroups.firstOrNull { group ->
                            val isSourceWithinBound = geoCalculator.distanceInKm(rideIntent.sourceLat, rideIntent.sourceLng, group.sourceLat, group.sourceLng) < MATCH_BOUND_DISTANCE_KM
                            val isDestinationWithinBound = geoCalculator.distanceInKm(rideIntent.destinationLat, rideIntent.destinationLng, group.destinationLat, group.destinationLng) < MATCH_BOUND_DISTANCE_KM

                            isSourceWithinBound && isDestinationWithinBound
                        }
                        if(foundMatchingGroup!=null) {
                            val members = rideGroupMemberRepository.findAllByRideGroupId(foundMatchingGroup.id!!)
                            val newMember = RideGroupMember(
                                rideGroupId = foundMatchingGroup.id,
                                rideIntentId = rideIntent.id!!
                            )
                            rideGroupMemberRepository.save(newMember)

                            val isFull = (members.size+1) == foundMatchingGroup.maxSize

                            rideGroupRepository.save(
                                foundMatchingGroup.copy(
                                    status = if(isFull) RideGroupStatus.FULL else RideGroupStatus.OPEN,
                                )
                            ) //TODO("Notify user that they have joined a group")
                        } else{
                            val searchingRideSearchProcesses = rideSearchProcessRepository.findAllByStatusAndIdNotLike(RideSearchProcessState.Searching, process.id!!)
                            val matchingRideSearchProcesses = searchingRideSearchProcesses.firstOrNull { process ->
                                val matchedRideIntent = rideIntentRepository.findById(process.rideIntentId).get()
                                val isSourceWithinBound = geoCalculator.distanceInKm(rideIntent.sourceLat, rideIntent.sourceLng, matchedRideIntent.sourceLat, matchedRideIntent.sourceLng) < MATCH_BOUND_DISTANCE_KM
                                val isDestinationWithinBound = geoCalculator.distanceInKm(rideIntent.destinationLat, rideIntent.destinationLng, matchedRideIntent.destinationLat, matchedRideIntent.destinationLng) < MATCH_BOUND_DISTANCE_KM
                                val isWithingTimeBound = when{
                                    rideIntent.scheduleType is ScheduleType.Future && matchedRideIntent.scheduleType is ScheduleType.Future -> {
                                        val rideIntentStartTime = Instant.parse(rideIntent.scheduleType.startTime)
                                        val matchedRideIntentStartTime = Instant.parse(matchedRideIntent.scheduleType.startTime)
                                        val doesTimeOverlaps = rideIntentStartTime.isAfter(matchedRideIntentStartTime) && rideIntentStartTime.isBefore(matchedRideIntentStartTime.plusSeconds(matchedRideIntent.flexibleMinutes*60L))
                                        doesTimeOverlaps
                                    }
                                    rideIntent.scheduleType is ScheduleType.Immediate && matchedRideIntent.scheduleType is ScheduleType.Immediate -> true
                                    else -> false
                                }
                                isSourceWithinBound && isDestinationWithinBound && isWithingTimeBound
                            }


                            val newGroup = RideGroup(
                                sourceLat = rideIntent.sourceLat,
                                sourceLng = rideIntent.sourceLng,
                                destinationLat = rideIntent.destinationLat,
                                destinationLng = rideIntent.destinationLng,
                                scheduleType = rideIntent.scheduleType
                            )
                            val rideGroupId = rideGroupRepository.save(newGroup).id!!
                            val member1 = RideGroupMember(
                                rideGroupId = rideGroupId,
                                rideIntentId = process.id
                            )
                            val member2 = RideGroupMember(
                                rideGroupId = rideGroupId,
                                rideIntentId = matchingRideSearchProcesses?.id!!
                            )
                            rideGroupMemberRepository.saveAll(listOf(member1, member2))
                        }

                    },
                    Duration.ofSeconds(3)
                )
            }
        }
    }
}