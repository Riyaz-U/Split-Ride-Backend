package com.wiseowl.splitride.feature.rideintent.model

import com.wiseowl.splitride.feature.auth.model.User
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "commutes")
data class Commute(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    val origin: String,
    val destination: String,
    val completionDate: Instant,
)

data class Amount(
    val amount: Int,
    val currency: String
)

data class Companion(
    val name: String
)