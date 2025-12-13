package com.wiseowl.splitride.feature.rideintent.service

import org.springframework.stereotype.Service

@Service
class AreaNormalizer {
    fun normalize(area: String): String{
        return area
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .replace(" +", " ")
            .replace(Regex("\\s+"), " ")      // collapse spaces
            .replace("sector", "sec")         // unify "sector" → "sec"
            .replace("phase", "ph")
            .replace("park", "pk")
            .replace("cyber hub", "cyberhub")
    }
}