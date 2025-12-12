package com.wiseowl.splitride.feature.user.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType

@Entity
data class User(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val company: String,
    val home: String,
    val officeArea: String,
    val createdAt: String
)
