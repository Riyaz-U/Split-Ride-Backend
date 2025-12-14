package com.wiseowl.splitride.feature.rideintent.util

import kotlin.test.Test

class GeoCalculatorTest {
    @Test
    fun `calculate geoDistance`() {
        val geoDistanceCalculator = GeoCalculator()
        val distanceInKm = geoDistanceCalculator.distanceInKm(
            lat1 = 28.657425,
            lng1 = 77.148814,
            lat2 = 28.645383,
            lng2 = 77.168881
        )
        println(distanceInKm)
        assert(distanceInKm == 2.372173522995905)
    }

    @Test
    fun `calculate angle`() {
        val geoDistanceCalculator = GeoCalculator()
        val angle = geoDistanceCalculator.angle(
            lat1 = 28.659567,
            lng1 = 77.145081,
            lat2 = 28.643034,
            lng2 = 77.137095
        )
        assert(angle == -115.78211900204673)
    }
}