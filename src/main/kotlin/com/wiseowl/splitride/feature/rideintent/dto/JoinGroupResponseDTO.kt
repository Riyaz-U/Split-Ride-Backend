package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.model.RideGroupMember
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
){
    companion object{
        fun RideGroupMember.toJoinGroupMemberDTO() = RideGroupMemberDTO(
            id = id,
            rideIntentId = rideIntentId,
            joinedAt = joinedAt
        )
    }
}
