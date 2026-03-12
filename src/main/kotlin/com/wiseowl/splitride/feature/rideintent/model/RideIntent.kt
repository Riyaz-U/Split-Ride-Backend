package com.wiseowl.splitride.feature.rideintent.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ride_intents")
data class RideIntent(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val sourceLat: Double,

    @Column(nullable = false)
    val sourceLng: Double,

    @Column(nullable = false)
    val destinationLat: Double,

    @Column(nullable = false)
    val destinationLng: Double,

    @Column(nullable = false)
    val flexibleMinutes: Int,

    @Column(nullable = false)
    val scheduleType: ScheduleType,

    @Column(nullable = false)
    @Enumerated(jakarta.persistence.EnumType.STRING)
    val status: RideIntentStatus = RideIntentStatus.ACTIVE,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
){
    protected constructor() : this(
        id = null,
        userId = UUID(0,0),
        sourceLat = 0.0,
        sourceLng = 0.0,
        destinationLat = 0.0,
        destinationLng = 0.0,
        scheduleType = ScheduleType.Immediate,
        flexibleMinutes = 0,
        status = RideIntentStatus.ACTIVE,
        createdAt = Instant.EPOCH
    )
}

enum class RideIntentStatus { ACTIVE, GROUPED, CANCELLED, EXPIRED, COMPLETED }

sealed interface ScheduleType{
    object Immediate: ScheduleType
    data class Future(val startTime: String): ScheduleType
}