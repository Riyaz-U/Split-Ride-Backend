package com.wiseowl.splitride.feature.rideintent.util

import org.springframework.stereotype.Component

@Component
class KeywordExtractor {

    fun extractKeywords(input: String): Set<String> {
        return input
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .map {
                when (it) {
                    "sector" -> "sec"
                    "phase" -> "ph"
                    "park" -> "pk"
                    "cyber", "cyberhub", "cybercity" -> "cyber"
                    else -> it
                }
            }
            .toSet()
    }
}