package com.wiseowl.splitride.feature.auth.job

import com.wiseowl.splitride.feature.auth.repository.RefreshTokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RefreshTokenFiltrationJob(
    private val refreshToken: RefreshTokenRepository
) {

    @Scheduled(fixedDelay = 1000)
    fun revokeExpiredToken(){
        refreshToken
    }
}