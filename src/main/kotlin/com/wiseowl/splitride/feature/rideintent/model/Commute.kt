package com.wiseowl.splitride.feature.rideintent.model

import com.wiseowl.splitride.feature.auth.model.User
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "commutes")
data class Commute(
    val id: UUID = UUID.randomUUID(),
    @OneToMany val user: User,
    val origin: String,
    val destination: String,
    val total: Amount,
    val saved: Amount,
    val completionDate: Instant,
    val companions: List<Companion>
)

data class Amount(
    val amount: Int,
    val currency: String
)

data class Companion(
    val name: String
)