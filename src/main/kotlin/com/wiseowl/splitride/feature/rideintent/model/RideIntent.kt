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
    @Enumerated(jakarta.persistence.EnumType.STRING)
    val direction: Direction,

    @Column(nullable = false)
    val sourceArea: String,

    @Column(nullable = false)
    val destinationArea: String,

    @Column(nullable = false)
    val normalizedSource: String,

    @Column(nullable = false)
    val normalizedDestination: String,

    @Column(nullable = false)
    val sourceLat: Double,

    @Column(nullable = false)
    val sourceLng: Double,

    @Column(nullable = false)
    val destinationLat: Double,

    @Column(nullable = false)
    val destinationLng: Double,

    @Column(nullable = false)
    val sourceKeywords: String,

    @Column(nullable = false)
    val destinationKeywords: String,

    @Column(nullable = false)
    val startTime: Instant,

    @Column(nullable = false)
    val flexibleMinutes: Int,

    @Column(nullable = false)
    @Enumerated(jakarta.persistence.EnumType.STRING)
    val status: RideIntentStatus = RideIntentStatus.ACTIVE,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
){
    protected constructor() : this(
        id = null,
        userId = UUID(0,0),
        direction = Direction.HOME_TO_OFFICE,
        sourceArea = "",
        destinationArea = "",
        normalizedSource = "",
        normalizedDestination = "",
        sourceLat = 0.0,
        sourceLng = 0.0,
        destinationLat = 0.0,
        destinationLng = 0.0,
        sourceKeywords = "",
        destinationKeywords = "",
        startTime = Instant.EPOCH,
        flexibleMinutes = 0,
        status = RideIntentStatus.ACTIVE,
        createdAt = Instant.EPOCH
    )
}

enum class Direction { HOME_TO_OFFICE, OFFICE_TO_HOME }

enum class RideIntentStatus { ACTIVE, GROUPED, CANCELLED, EXPIRED, COMPLETED }
