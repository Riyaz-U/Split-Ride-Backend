package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.model.Direction

data class CreateRideIntentRequestDTO(
    val userId: String,
    val direction: Direction,
    val sourceArea: String,
    val destinationArea: String,
    val startTime: String,  // ISO format
    val flexibleMinutes: Int
)