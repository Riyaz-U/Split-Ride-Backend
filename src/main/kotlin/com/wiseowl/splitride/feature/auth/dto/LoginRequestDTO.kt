package com.wiseowl.splitride.feature.auth.dto

import jakarta.annotation.Nonnull

data class LoginRequestDTO(
    @Nonnull val email: String,
    @Nonnull val password: String
)
