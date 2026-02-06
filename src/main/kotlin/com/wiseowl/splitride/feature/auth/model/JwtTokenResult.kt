package com.wiseowl.splitride.feature.auth.model

class JwtTokenResult(
    val refreshToken: String,
    val accessToken: String,
    val accessTokenExpiresInSec: Long
)