package com.wiseowl.splitride.feature.rideintent.dto

import java.time.Instant
import java.util.UUID

data class JoinGroupResponseDTO(
    val rideGroupId: UUID,
    val currentMembers: List<RideGroupMemberDTO>,
    val isGroupFull: Boolean
)

data class RideGroupMemberDTO(
    val id: UUID? = null,
    val rideIntentId: UUID,
    val joinedAt: Instant = Instant.now()
)
