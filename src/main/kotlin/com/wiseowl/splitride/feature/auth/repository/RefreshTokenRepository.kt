package com.wiseowl.splitride.feature.auth.repository

import com.wiseowl.splitride.feature.auth.model.RefreshToken
import com.wiseowl.splitride.feature.auth.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findAllByUserAndRevoked(user: User, revoked: Boolean): List<RefreshToken>
    fun findByTokenHash(tokenHash: String): RefreshToken
}