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
    val startTime: Instant,

    @Column(nullable = false)
    val flexibleMinutes: Int,

    @Column(nullable = false)
    @Enumerated(jakarta.persistence.EnumType.STRING)
    val status: RideIntentStatus = RideIntentStatus.ACTIVE,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
){
    constructor() : this(
        id = null,
        userId = UUID.randomUUID(),
        direction = Direction.HOME_TO_OFFICE,
        sourceArea = "",
        destinationArea = "",
        normalizedSource = "",
        normalizedDestination = "",
        startTime = Instant.now(),
        flexibleMinutes = 0,
        status = RideIntentStatus.ACTIVE,
        createdAt = Instant.now()
    )
}

enum class Direction { HOME_TO_OFFICE, OFFICE_TO_HOME }

enum class RideIntentStatus { ACTIVE, CANCELLED, EXPIRED }
