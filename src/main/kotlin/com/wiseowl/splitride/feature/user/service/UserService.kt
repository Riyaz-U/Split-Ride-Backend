package com.wiseowl.splitride.feature.user.service

import com.wiseowl.splitride.feature.user.dto.RegisterRequestDTO
import com.wiseowl.splitride.feature.user.model.User
import com.wiseowl.splitride.feature.user.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    private val encoder = BCryptPasswordEncoder()

    fun createUser(req: RegisterRequestDTO): User {
        if (userRepository.existsByEmail(req.email)) {
            throw IllegalArgumentException("Email already in use")
        }

        val hashedPassword = encoder.encode(req.password)!!

        val user = User(
            firstName = req.firstName,
            lastName = req.lastName,
            email = req.email,
            passwordHash = hashedPassword, // implement a dummy hash for now
            company = req.company,
            homeArea = req.homeArea,
            officeArea = req.officeArea
        )

        return userRepository.save(user)
    }

    fun getUserById(id: UUID): User =
        userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User not found") }
}