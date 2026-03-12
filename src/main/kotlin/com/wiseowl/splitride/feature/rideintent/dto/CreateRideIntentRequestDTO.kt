package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.model.ScheduleType

data class CreateRideIntentRequestDTO(
    val sourceLat: Double,
    val sourceLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val scheduleType: ScheduleType,  // ISO format
    val flexibleMinutes: Int
)