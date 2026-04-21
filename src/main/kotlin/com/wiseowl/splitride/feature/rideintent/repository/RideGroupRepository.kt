package com.wiseowl.splitride.feature.rideintent.repository

import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RideGroupRepository: JpaRepository<RideGroup, UUID>{
    fun findAllByStatusIn(
        status: List<RideGroupStatus>
    ): List<RideGroup>
    fun findAllByStatus(status: RideGroupStatus): List<RideGroup>
}