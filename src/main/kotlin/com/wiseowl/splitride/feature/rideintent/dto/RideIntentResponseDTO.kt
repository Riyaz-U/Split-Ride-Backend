package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus

data class RideIntentResponseDTO(
    val id: String,
    val userId: String,
    val flexibleMinutes: Int,
    val status: RideIntentStatus,
    val createdAt: String
)

fun RideIntent.toDTO(): RideIntentResponseDTO {
    return RideIntentResponseDTO(
        id = id.toString(),
        userId = userId.toString(),
        flexibleMinutes = flexibleMinutes,
        status = status,
        createdAt = createdAt.toString()
    )
}