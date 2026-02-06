package com.wiseowl.splitride.feature.auth.service

import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Component
class TokenHasher {
    fun encode(token: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(token.toByteArray(StandardCharsets.UTF_8))

            val hex = StringBuilder()
            for (b in hash) {
                hex.append(String.format("%02x", b))
            }
            return hex.toString()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to hash token", e)
        }
    }
}