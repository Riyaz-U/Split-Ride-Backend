package com.wiseowl.splitride.feature.rideintent.dto

import com.wiseowl.splitride.feature.rideintent.dto.RideGroupMemberDTO.Companion.toJoinGroupMemberDTO
import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.model.RideGroupMember
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus

data class RideGroupDTO(
    val sourceLat: Double,
    val sourceLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val members: List<RideGroupMemberDTO>,
    val status: RideGroupStatus
){
    companion object{
        fun RideGroup.toRideGroupDTO(members: List<RideGroupMember>): RideGroupDTO {
            return RideGroupDTO(
                sourceLat = sourceLat,
                sourceLng = sourceLng,
                destinationLat = destinationLat,
                destinationLng = destinationLng,
                members = members.map { it.toJoinGroupMemberDTO() },
                status = status
            )
        }
    }
}