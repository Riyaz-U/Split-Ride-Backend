package com.wiseowl.splitride.feature.user.service

import com.wiseowl.splitride.feature.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
final class UserService(private val userRepository: UserRepository) {
    final fun doestUserWithEmailExists(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun createUser(user: User): User {

    }
}