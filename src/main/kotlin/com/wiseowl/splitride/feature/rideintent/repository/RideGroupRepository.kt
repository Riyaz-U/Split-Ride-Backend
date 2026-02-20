package com.wiseowl.splitride.feature.rideintent.repository

import com.wiseowl.splitride.feature.rideintent.model.Direction
import com.wiseowl.splitride.feature.rideintent.model.RideGroup
import com.wiseowl.splitride.feature.rideintent.model.RideGroupStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface RideGroupRepository: JpaRepository<RideGroup, UUID>{
    fun findByDirectionAndStartTimeBucket(
        direction: Direction,
        startTimeBucket: Instant
    ): List<RideGroup>

    fun findAllByStartTimeBucketBeforeAndStatusIn(
        currentTime: Instant,
        status: List<RideGroupStatus>
    ): List<RideGroup>

    fun findRideGroupsBy(id: UUID): RideGroup

    fun findAllByStatusOrderByStartTimeBucketAsc(status: RideGroupStatus): List<RideGroup>
}