package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.dto.RideGroupMemberDTO.Companion.toJoinGroupMemberDTO
import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.model.RideGroupMember
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus

data class RideGroupDTO(
    val direction: Direction,
    val sourceLat: Double,
    val sourceLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val startTime: String,  // ISO format
    val members: List<RideGroupMemberDTO>,
    val status: RideGroupStatus
){
    companion object{
        fun RideGroup.toRideGroupDTO(members: List<RideGroupMember>): RideGroupDTO {
            return RideGroupDTO(
                direction = direction,
                sourceLat = sourceLat,
                sourceLng = sourceLng,
                destinationLat = destinationLat,
                destinationLng = destinationLng,
                startTime = startTimeBucket.toString(),
                members = members.map { it.toJoinGroupMemberDTO() },
                status = status
            )
        }
    }
}