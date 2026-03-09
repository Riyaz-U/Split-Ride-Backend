package com.wiseowl.splitride.feature.auth.service

import com.wiseowl.splitride.exception.BadCredentialsException
import com.wiseowl.splitride.feature.auth.dto.AuthenticationResponseDTO
import com.wiseowl.splitride.feature.auth.dto.LoginRequestDTO
import com.wiseowl.splitride.feature.auth.dto.LogoutRequestDTO
import com.wiseowl.splitride.feature.auth.dto.RefreshTokenRequestDTO
import com.wiseowl.splitride.feature.auth.dto.RefreshTokenResponseDTO
import com.wiseowl.splitride.feature.auth.dto.RegisterRequestDTO
import com.wiseowl.splitride.feature.auth.model.User
import com.wiseowl.splitride.feature.auth.repository.RefreshTokenRepository
import com.wiseowl.splitride.feature.auth.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JWTService
) {

    private val encoder = BCryptPasswordEncoder()

    fun register(req: RegisterRequestDTO) {
        if (userRepository.existsByEmail(req.email)) {
            throw IllegalArgumentException("Email already in use")
        }

        val hashedPassword = encoder.encode(req.password)!!

        userRepository.save(User(
            firstName = req.firstName,
            lastName = req.lastName,
            email = req.email,
            passwordHash = hashedPassword
        ))
    }

    fun login(req: LoginRequestDTO): AuthenticationResponseDTO {
        val user = userRepository.findByEmail(req.email)
            ?: throw BadCredentialsException("Email or Password is incorrect")

        val isPasswordCorrect = !encoder.matches(req.password, user.passwordHash)
        if(isPasswordCorrect) throw BadCredentialsException("Email or Password is incorrect")

        val token = jwtService.generateToken(user)

        val response = AuthenticationResponseDTO(
            accessToken = token.accessToken,
            accessTokenExpirationSec = token.accessTokenExpiresInSec,
            refreshToken = token.refreshToken
        )

        return response
    }

    fun refreshToken(req: RefreshTokenRequestDTO): Result<RefreshTokenResponseDTO> {
        val refreshToken = refreshTokenRepository.findByIdOrNull(req.refreshToken)
            ?: return Result.failure(IllegalArgumentException("RefreshToken is not valid"))

        if(refreshToken.revoked) throw IllegalArgumentException("RefreshToken revoked")
        val user = refreshToken.user
        val accessToken = jwtService.getRefreshedAccessToken(user)
        val response = RefreshTokenResponseDTO(accessToken = accessToken)

        return Result.success(response)
    }

    fun processLogout(
        userId: UUID,
        req: LogoutRequestDTO
    ){
        if(req.refreshToken==null){
            val user = userRepository.findByIdOrNull(userId) ?: throw IllegalArgumentException("User not found")
            val refreshTokens = refreshTokenRepository.findAllByUserAndRevoked(user, false)
            refreshTokenRepository.saveAll(refreshTokens.map { it.copy(revoked = true) })
        } else{
            val tokenHash = encoder.encode(req.refreshToken)!!
            val refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            refreshTokenRepository.save(refreshToken.copy(revoked = true))
        }
    }
}