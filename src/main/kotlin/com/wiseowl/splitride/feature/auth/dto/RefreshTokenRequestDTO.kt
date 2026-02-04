package com.wiseowl.splitride.feature.auth.dto

import java.util.UUID

data class RefreshTokenRequestDTO(
    val refreshToken: UUID
)
