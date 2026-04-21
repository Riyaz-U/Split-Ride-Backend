package com.wiseowl.splitride.job

import com.wiseowl.splitride.config.Configuration
import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
import com.wiseowl.splitride.feature.rideintent.repository.RideSearchProcessRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class RideIntentStatusUpdateJob(
    val repository: RideIntentRepository
) {
    @Scheduled(cron = "0 * * * * *")
    fun updateExpiredIntent() {
        val now = Instant.now()
        val logicallyExpiredIntents = repository.findAll()
            .filter {
                it.status != RideIntentStatus.ACTIVE &&
                        it.status != RideIntentStatus.GROUPED &&
                        Configuration.ScheduleTypeImmediateExpirationSeconds > now.epochSecond - it.createdAt.epochSecond
            }
        repository.deleteAll(logicallyExpiredIntents)
    }
}