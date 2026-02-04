package com.wiseowl.splitride.feature.auth.dto

data class AuthenticationResponseDTO(
    val accessToken: String,
    val accessTokenExpirationSec: Long,
    val refreshToken: String
)
