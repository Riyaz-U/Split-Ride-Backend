package com.wiseowl.splitride.feature.rideintent.util

import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TimerBucket {
    fun get(time: Instant, minutes: Long = 10): Instant {
        val epoch = time.epochSecond
        val bucket = (epoch / (minutes * 60)) * (minutes * 60)
        return Instant.ofEpochSecond(bucket)
    }
}