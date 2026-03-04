package com.wiseowl.splitride.job

import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class RideGroupStatusUpdateJob(
    val rideGroupRepository: RideGroupRepository,
) {
    @Scheduled(cron = "0 * * * * *")
    fun updateExpiredGroup() {
        val now = Instant.now()
        val activeRideGroups = rideGroupRepository.findAllByStartTimeBucketBeforeAndStatusIn(now, listOf(RideGroupStatus.FULL, RideGroupStatus.OPEN))
        val updatedRideGroups = activeRideGroups.map { it.copy(
            status = if(it.status == RideGroupStatus.FULL) RideGroupStatus.COMPLETED else RideGroupStatus.CANCELLED
        ) }
        rideGroupRepository.saveAll(updatedRideGroups)
    }
}