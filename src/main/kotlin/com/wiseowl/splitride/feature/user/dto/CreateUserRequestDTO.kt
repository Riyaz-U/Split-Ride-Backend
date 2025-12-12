package com.wiseowl.splitride.feature.user.dto

import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Value

data class CreateUserRequestDTO(
    @NotBlank(message = "first name is required")
    val firstName: String,
    @NotBlank(message = "last name is required")
    val lastName: String,
    @NotBlank(message = "password is required")
    val password: String,
    @NotBlank(message = "email is required")
    val email: String
)
