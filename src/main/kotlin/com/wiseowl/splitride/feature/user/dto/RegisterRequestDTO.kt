package com.wiseowl.splitride.feature.user.dto

import jakarta.validation.constraints.NotBlank

data class RegisterRequestDTO(
    @NotBlank val firstName: String,
    @NotBlank val lastName: String,
    @NotBlank val email: String,
    @NotBlank val password: String,
    @NotBlank val company: String,
    @NotBlank val homeArea: String,
    @NotBlank val officeArea: String
)
