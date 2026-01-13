package com.wiseowl.splitride.feature.rideintent.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ride_groups")
data class RideGroup(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val direction: Direction,

    @Column(nullable = false)
    val sourceLat: Double,

    @Column(nullable = false)
    val sourceLng: Double,

    @Column(nullable = false)
    val destinationLat: Double,

    @Column(nullable = false)
    val destinationLng: Double,

    @Column(nullable = false)
    val startTimeBucket: Instant,

    @Column(nullable = false)
    val maxSize: Int = 3,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
){
    constructor(): this(
        direction = Direction.HOME_TO_OFFICE,
        sourceLat = 0.0,
        sourceLng = 0.0,
        destinationLat = 0.0,
        destinationLng = 0.0,
        startTimeBucket = Instant.now(),
        maxSize = 3,
        createdAt = Instant.now()
    )
}