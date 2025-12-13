package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus

data class RideIntentResponseDTO(
    val id: String,
    val userId: String,
    val direction: Direction,
    val sourceArea: String,
    val destinationArea: String,
    val startTime: String,
    val flexibleMinutes: Int,
    val status: RideIntentStatus,
    val createdAt: String
)

fun RideIntent.toDTO(): RideIntentResponseDTO {
    return RideIntentResponseDTO(
        id = id.toString(),
        userId = userId.toString(),
        direction = direction,
        sourceArea = sourceArea,
        destinationArea = destinationArea,
        startTime = startTime.toString(),
        flexibleMinutes = flexibleMinutes,
        status = status,
        createdAt = createdAt.toString()
    )
}