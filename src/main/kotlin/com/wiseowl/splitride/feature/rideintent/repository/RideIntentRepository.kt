package com.wiseowl.splitride.feature.rideintent.repository

import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import com.wiseowl.splitride.feature.rideintent.model.RideIntentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RideIntentRepository: JpaRepository<RideIntent, UUID>{
    fun findByIdAndStatus(id: UUID, status: RideIntentStatus): RideIntent?
    fun findByIdAndUserId(id: UUID, userId: UUID): RideIntent?
    fun findAllByUserId(userId: UUID): List<RideIntent>
}