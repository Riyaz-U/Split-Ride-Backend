package com.wiseowl.splitride.feature.auth.dto

import jakarta.validation.constraints.NotBlank

data class RegisterRequestDTO(
    @NotBlank val firstName: String,
    @NotBlank val lastName: String,
    @NotBlank val email: String,
    @NotBlank val password: String
)