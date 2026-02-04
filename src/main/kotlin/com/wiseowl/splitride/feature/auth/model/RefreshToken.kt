package com.wiseowl.splitride.feature.auth.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @ManyToOne(optional = false)
    val user: User,

    @Column(nullable = false)
    val tokenHash: String,

    @Column(nullable = false)
    val expiresAt: Instant,

    @Column(nullable = false)
    val revoked: Boolean,
    val replacedByTokenCreatedAt: Instant
)
