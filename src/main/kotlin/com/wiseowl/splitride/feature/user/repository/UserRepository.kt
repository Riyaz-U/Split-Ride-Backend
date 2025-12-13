package com.wiseowl.splitride.feature.user.repository

import com.wiseowl.splitride.feature.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun existsByEmail(email: String): Boolean
}