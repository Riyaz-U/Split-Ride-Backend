package com.wiseowl.splitride.feature.rideintent.repository

import com.wiseowl.splitride.feature.rideintent.model.Commute
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CommuteRepository: JpaRepository<Commute, UUID> {
//    fun findAllByUserId(userId: UUID): List<Commute>
}