package com.rideassistant.parse

object RideNormalizer {
    fun metersToKm(meters: Double): Double = (meters / 1000.0)

    fun roundToTwoDecimals(value: Double): Double =
        kotlin.math.round(value * 100.0) / 100.0

    fun cleanLocationText(raw: String?): String? {
        val trimmed = raw?.trim()
        return if (trimmed.isNullOrEmpty() || trimmed.length < 3) null else trimmed
    }
}
