package com.wiseowl.splitride.feature.rideintent.repository

import com.wiseowl.splitride.feature.rideintent.model.RideGroupMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RideGroupMemberRepository: JpaRepository<RideGroupMember, UUID>{
    fun findAllByRideGroupId(rideGroupId: UUID): List<RideGroupMember>
    fun findByRideGroupIdAndRideIntentId(rideGroupId: UUID, rideIntentId: UUID): RideGroupMember?
}