package com.rideassistant.intelligence.home

import kotlin.math.*

data class HomeDistanceResult(
    val distanceKm: Double?,
    val distanceType: String = "Straight-line distance",
    val displayLabel: String
)

object HomeDistanceCalculator {
    private const val EARTH_RADIUS_KM = 6371.0

    fun isValidCoordinate(lat: Double?, lon: Double?): Boolean {
        if (lat == null || lon == null) return false
        if (lat.isNaN() || lon.isNaN() || lat.isInfinite() || lon.isInfinite()) return false
        return lat in -90.0..90.0 && lon in -180.0..180.0
    }

    fun calculateHaversineKm(
        lat1: Double?,
        lon1: Double?,
        lat2: Double?,
        lon2: Double?
    ): Double? {
        if (!isValidCoordinate(lat1, lon1) || !isValidCoordinate(lat2, lon2)) return null
        val dLat = Math.toRadians(lat2!! - lat1!!)
        val dLon = Math.toRadians(lon2!! - lon1!!)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) +
            cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return roundToTwoDecimals(EARTH_RADIUS_KM * c)
    }

    fun formatDistance(distanceKm: Double?): HomeDistanceResult {
        if (distanceKm == null || distanceKm < 0.0 || distanceKm.isNaN()) {
            return HomeDistanceResult(
                distanceKm = null,
                displayLabel = "UNKNOWN"
            )
        }
        val label = if (distanceKm < 1.0) {
            val meters = (distanceKm * 1000).roundToInt()
            "$meters m from Home (Straight-line)"
        } else {
            val formatted = String.format("%.1f", distanceKm)
            "$formatted km from Home (Straight-line)"
        }
        return HomeDistanceResult(
            distanceKm = distanceKm,
            displayLabel = label
        )
    }

    private fun roundToTwoDecimals(value: Double): Double =
        round(value * 100.0) / 100.0
}
