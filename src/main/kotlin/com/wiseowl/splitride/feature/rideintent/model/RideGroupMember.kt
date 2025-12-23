package com.wiseowl.splitride.feature.rideintent.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ride_group_members")
data class RideGroupMember(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val rideGroupId: UUID,

    @Column(nullable = false)
    val rideIntentId: UUID,

    @Column(nullable = false)
    val joinedAt: Instant = Instant.now()
){
    protected constructor() : this(
        id = null,
        rideGroupId = UUID(0,0),
        rideIntentId = UUID(0,0),
        joinedAt = Instant.EPOCH
    )
}