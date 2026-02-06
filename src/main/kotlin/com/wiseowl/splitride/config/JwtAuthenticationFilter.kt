package com.wiseowl.splitride.config

import com.wiseowl.splitride.config.model.AuthenticatedUser
import com.wiseowl.splitride.feature.auth.service.JWTService
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JWTService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        println("validating init")
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substringAfter("Bearer ").trim()

        try {
            println("validating token: $token")
            val claims = jwtService.parseAndValidate(token)

            val principal = AuthenticatedUser(claims.userId)

            val authentication = UsernamePasswordAuthenticationToken(
                principal,
                null,
                emptyList()
            )

            SecurityContextHolder.getContext().authentication = authentication
            println("validating done")
        } catch (ex: JwtException) {
            println("validating error: ${ex.message}")
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}