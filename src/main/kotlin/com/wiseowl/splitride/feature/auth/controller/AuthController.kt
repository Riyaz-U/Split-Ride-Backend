package com.wiseowl.splitride.feature.auth.controller

import com.wiseowl.splitride.feature.auth.dto.*
import com.wiseowl.splitride.feature.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID


@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequestDTO): ResponseEntity<Unit> {
        authService.register(req)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequestDTO): ResponseEntity<AuthenticationResponseDTO> {
        val authResult = authService.login(req)

        return ResponseEntity.ok(
            authResult
        )
    }

    @PostMapping("/refresh-token")
    fun refreshToken(@RequestBody req: RefreshTokenRequestDTO): ResponseEntity<RefreshTokenResponseDTO> {
        val responseBody = authService.refreshToken(req).getOrNull()
        return if(responseBody == null) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        } else ResponseEntity.ok(responseBody)
    }

    @GetMapping("/logout")
    fun logout(@RequestBody req: LogoutRequestDTO): ResponseEntity<Unit> {
        authService.processLogout(userId = UUID.randomUUID(), req = req)
        return ResponseEntity.ok().build()
    }
}
