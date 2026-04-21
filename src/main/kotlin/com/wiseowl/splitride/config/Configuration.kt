package com.wiseowl.splitride.config


object Configuration {
    val ScheduleTypeImmediateExpirationSeconds: Long
        get() = 60*10L //10 minutes

    const val RideGroupExpirationSeconds: Long = 60*60*24*30 // 30 days
}