package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.model.Direction
import java.util.UUID

data class JoinGroupRequestDTO(
    val rideIntentId: UUID,
    val direction: Direction,
    val sourceLat: Double,
    val sourceLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val time: String
)
