package com.wiseowl.splitride.feature.auth.model

import jakarta.persistence.*
import org.hibernate.validator.constraints.Range
import java.time.Instant
import java.util.*

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val firstName: String,

    @Column(nullable = false)
    val lastName: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val passwordHash: String,

    @Range(min = 0, max = 5)
    @Column(nullable = true)
    val rating: Double? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)