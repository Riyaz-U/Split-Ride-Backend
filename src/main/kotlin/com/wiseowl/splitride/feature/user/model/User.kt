package com.wiseowl.splitride.feature.user.model

import jakarta.persistence.*
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

    @Column(nullable = true)
    val company: String? = null,

    @Column(nullable = true)
    val homeArea: String? = null,

    @Column(nullable = true)
    val officeArea: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)