package com.wiseowl.splitride.feature.user.dto

import com.wiseowl.splitride.feature.user.model.User

data class UserResponseDTO(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val company: String?,
    val homeArea: String?,
    val officeArea: String?,
    val createdAt: String
)

fun User.toDTO() = UserResponseDTO(
    id = this.id.toString(),
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    company = this.company,
    homeArea = this.homeArea,
    officeArea = this.officeArea,
    createdAt = this.createdAt.toString()
)

