package com.wiseowl.splitride.job

import com.wiseowl.splitride.config.Configuration
import com.wiseowl.splitride.config.Configuration.ScheduleTypeImmediateExpirationSeconds
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.model.ScheduleType
import com.wiseowl.splitride.feature.rideintent.repository.RideGroupRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class RideGroupStatusUpdateJob(
    val rideGroupRepository: RideGroupRepository,
) {
    @Scheduled(cron = "0 * * * * *")
    fun deleteOldGroup() {
        val now = Instant.now()
        val expiredGroups = rideGroupRepository.findAllByStatusIn(listOf(RideGroupStatus.COMPLETED, RideGroupStatus.CANCELLED)).filter {
            Configuration.RideGroupExpirationSeconds > now.epochSecond - it.createdAt.epochSecond
        }
        rideGroupRepository.deleteAll(expiredGroups)
    }
}