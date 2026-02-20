package com.wiseowl.splitride.job

import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import com.wiseowl.splitride.feature.rideintent.repository.RideIntentRepository
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
        val activeRideIntent = repository.findAllByStartTimeBeforeAndStatusIs(now, RideIntentStatus.ACTIVE)
        repository.saveAll(activeRideIntent.filter { it.startTime.plusSeconds(it.flexibleMinutes*60L) <= now }.map { it.copy(status = RideIntentStatus.EXPIRED) })
    }
}