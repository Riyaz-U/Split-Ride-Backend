package com.wiseowl.splitride.feature.user.repository

import com.wiseowl.splitride.feature.user.model.User
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: org.springframework.data.repository.Repository<User, String> {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}