package com.wiseowl.splitride.feature.rideintent.util

import org.springframework.util.Assert
import kotlin.test.Test

class AreaNormalizerTest {
    val areaNormalizer = AreaNormalizer()

    @Test
    fun `normalizes all keywords`(){
        val areas = arrayListOf(
            "DlF",
            "city park",
            "Cyber City"
        )
        val normalizeAreas = arrayListOf(
            "dlf",
            "city pk",
            "cybercity"
        )

        areas.forEach {
            val normalizedArea = areaNormalizer.normalize(it)
            Assert.isTrue(normalizeAreas.contains(normalizedArea), "normalized area $normalizedArea is not found")
        }
    }
}