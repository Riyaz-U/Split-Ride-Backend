package com.wiseowl.splitride.feature.rideintent.util

import org.springframework.stereotype.Component
import java.lang.Math.toDegrees
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

@Component
class GeoCalculator {
    fun distanceInKm(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c/1000.0
    }

    fun angle(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        return toDegrees(
            atan2(
                lat2 - lat1,
                lng2 - lng1
            )
        )
    }
}