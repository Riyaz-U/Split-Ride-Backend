package com.wiseowl.splitride.feature.rideintent.util

import org.springframework.stereotype.Component

@Component
class AreaNormalizer {
    fun normalize(area: String): String{
        var x = area
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ") // Remove punctuation
            .replace(Regex("\\s+"), " ")       // Collapse spaces

        // Standard abbreviations
        x = x.replace("sector", "sec")
        x = x.replace("phase", "ph")
        x = x.replace("park", "pk")
        x = x.replace("techpk", "tech pk")
        x = x.replace("techpark", "tech pk")
        x = x.replace("tech park", "tech pk")

        // CyberHub / Cyber City handling
        x = x.replace("cyber hub", "cyberhub")
        x = x.replace("cyber city", "cybercity")
        x = x.replace("cybercity", "cybercity")
        x = x.replace("cyberhub", "cyberhub")

        // Normalize combinations
        x = x.replace("dlf cybercity", "cyberhub")
        x = x.replace("dlf cyber hub", "cyberhub")

        return x.trim()
    }
}