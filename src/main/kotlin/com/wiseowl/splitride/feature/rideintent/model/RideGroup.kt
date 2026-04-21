package com.wiseowl.splitride.feature.rideintent.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ride_groups")
data class RideGroup(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val sourceLat: Double,

    @Column(nullable = false)
    val sourceLng: Double,

    @Column(nullable = false)
    val destinationLat: Double,

    @Column(nullable = false)
    val destinationLng: Double,

    @Column(nullable = false)
    @Convert(converter = ScheduleTypeConverter::class)
    val scheduleType: ScheduleType,

    @Column(nullable = false)
    val maxSize: Int = 3,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    val status: RideGroupStatus = RideGroupStatus.OPEN
){
    constructor(): this(
        sourceLat = 0.0,
        sourceLng = 0.0,
        destinationLat = 0.0,
        destinationLng = 0.0,
        scheduleType = ScheduleType.Immediate,
        maxSize = 3,
        createdAt = Instant.now()
    )
}

enum class RideGroupStatus{
    OPEN, FULL, CLOSED, COMPLETED, CANCELLED
}