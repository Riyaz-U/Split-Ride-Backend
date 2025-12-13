package com.wiseowl.splitride.feature.user.controller

import com.wiseowl.splitride.feature.user.dto.RegisterRequestDTO
import com.wiseowl.splitride.feature.user.dto.UserResponseDTO
import com.wiseowl.splitride.feature.user.dto.toDTO
import com.wiseowl.splitride.feature.user.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun register(@Valid @RequestBody req: RegisterRequestDTO): UserResponseDTO {
        val saved = userService.createUser(req)
        return saved.toDTO()
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID): UserResponseDTO {
        val user = userService.getUserById(id)
        return user.toDTO()
    }
}
