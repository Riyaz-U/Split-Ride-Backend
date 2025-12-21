package com.wiseowl.splitride.feature.rideintent.repository

import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RideGroupRepository: JpaRepository<RideGroup, UUID>