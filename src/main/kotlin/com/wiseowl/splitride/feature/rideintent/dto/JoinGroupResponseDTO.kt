package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.model.RideGroupMember
import java.util.UUID

data class JoinGroupResponseDTO(
    val rideGroupId: UUID,
    val currentMembers: List<RideGroupMember>,
    val isGroupFull: Boolean
)
