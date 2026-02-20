package com.wiseowl.splitride.feature.auth.controller

import com.wiseowl.splitride.feature.auth.dto.*
import com.wiseowl.splitride.feature.auth.service.AuthService
import com.wiseowl.splitride.feature.response.SplitRideResponse
import com.wiseowl.splitride.feature.response.SplitRideResponse.Companion.createErrorResponse
import com.wiseowl.splitride.feature.response.SplitRideResponse.Companion.createSuccessResponse
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
    fun register(@Valid @RequestBody req: RegisterRequestDTO): SplitRideResponse<Unit> {
        authService.register(req)
        return createSuccessResponse(Unit,HttpStatus.CREATED.value())
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequestDTO): SplitRideResponse<AuthenticationResponseDTO> {
        val authResult = authService.login(req)
        return createSuccessResponse(authResult,HttpStatus.OK.value())
    }

    @PostMapping("/refresh-token")
    fun refreshToken(@RequestBody req: RefreshTokenRequestDTO): SplitRideResponse<RefreshTokenResponseDTO?> {
        val responseBody = authService.refreshToken(req).getOrNull()
        return if(responseBody == null) {
            createErrorResponse(
                errorMessage = "Invalid refresh token",
                status = HttpStatus.UNAUTHORIZED.value()
            )
        } else createSuccessResponse(responseBody, HttpStatus.OK.value())
    }

    @GetMapping("/logout")
    fun logout(@RequestBody req: LogoutRequestDTO): ResponseEntity<Unit> {
        authService.processLogout(userId = UUID.randomUUID(), req = req)
        return ResponseEntity.ok().build()
    }
}
