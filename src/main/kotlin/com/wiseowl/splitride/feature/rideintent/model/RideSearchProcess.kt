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
@Table(name = "ride_search_process")
data class RideSearchProcess(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val rideIntentId: UUID,

    @Column(nullable = false)
    @Convert(converter = RideSearchProcessStateConverter::class)
    val status: RideSearchProcessState = RideSearchProcessState.Idle
)