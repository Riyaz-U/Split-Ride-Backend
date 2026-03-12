package com.wiseowl.splitride.feature.rideintent.repository

import com.wiseowl.splitride.feature.rideintent.model.RideSearchProcess
import com.wiseowl.splitride.feature.rideintent.model.RideSearchProcessState
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RideSearchProcessRepository: JpaRepository<RideSearchProcess, UUID>  {
    fun findAllByStatus(status: RideSearchProcessState): List<RideSearchProcess>
    fun findAllByStatusAndIdNotLike(status: RideSearchProcessState, id: UUID): List<RideSearchProcess>
}