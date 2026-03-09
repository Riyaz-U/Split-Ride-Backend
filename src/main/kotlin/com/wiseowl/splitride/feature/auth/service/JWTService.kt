package com.wiseowl.splitride.feature.auth.service

import com.wiseowl.splitride.feature.auth.model.JwtClaims
import com.wiseowl.splitride.feature.auth.model.JwtTokenResult
import com.wiseowl.splitride.feature.auth.model.RefreshToken
import com.wiseowl.splitride.feature.auth.model.User
import com.wiseowl.splitride.feature.auth.repository.RefreshTokenRepository
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class JWTService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val hasher: TokenHasher
) {
   private var key: SecretKey = Jwts.SIG.HS256.key().build()

    private val refreshTokenExpirationMilliSeconds: Long = 1000*60*60*24*30
    private val accessTokenExpirationMilliSeconds: Long  = 1000*60*60

    fun generateToken(user: User): JwtTokenResult {
        val refreshToken = generateRefreshToken(user)
        val accessToken = generateAccessToken(user)

        return JwtTokenResult(
            accessToken = accessToken,
            accessTokenExpiresInSec = accessTokenExpirationMilliSeconds,
            refreshToken = refreshToken
        )
    }

    fun getRefreshedAccessToken(user: User): String {
        val accessToken = generateAccessToken(user)
        return accessToken
    }

    private fun generateRefreshToken(user: User): String {
        val refreshTokenString = Jwts.builder()
                .subject(user.email)
                .claims(parseClaims(user))
                .expiration(Date(System.currentTimeMillis() + refreshTokenExpirationMilliSeconds))
                .signWith(key)
                .compact()

        val hashedRefreshToken = hasher.encode(refreshTokenString)

        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                tokenHash = hashedRefreshToken,
                expiresAt = Instant.now().plusSeconds(refreshTokenExpirationMilliSeconds),
                revoked = false,
                replacedByTokenCreatedAt = Instant.now()
            )
        )
        return refreshTokenString
    }

    private fun generateAccessToken(user: User): String {
        return Jwts.builder()
            .subject(user.id.toString())
            .claims(parseClaims(user))
            .expiration(Date(System.currentTimeMillis() + accessTokenExpirationMilliSeconds))
            .signWith(key)
            .compact()
    }



    fun parseAndValidate(token: String): JwtClaims {
        val claims = Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            .payload

        return JwtClaims(
            userId = claims.subject
        )
    }

    private fun parseClaims(user: User): MutableMap<String, Any> {
        return mutableMapOf(
            "first_name" to user.firstName,
            "last_name" to user.lastName
        )
    }
}