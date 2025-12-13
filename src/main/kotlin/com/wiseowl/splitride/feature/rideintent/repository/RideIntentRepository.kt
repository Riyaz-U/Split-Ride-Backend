package com.wiseowl.splitride.feature.rideintent.repository

import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideIntent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RideIntentRepository: JpaRepository<RideIntent, UUID>{
    fun findAllByDirection(direction: Direction): List<RideIntent>
}