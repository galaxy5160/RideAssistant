package com.rideassistant.intelligence.home

import com.rideassistant.parse.RideOffer
import javax.inject.Inject
import javax.inject.Singleton

enum class OperatingMode {
    NORMAL,
    HOME_PRIORITY,
    CUSTOM_DESTINATION
}

enum class HomeMatchLevel {
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN
}

data class HomeZone(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double = 5.0
)

data class HomePriorityScore(
    val score: Int,
    val matchLevel: HomeMatchLevel,
    val distanceResult: HomeDistanceResult,
    val explanation: String
)

@Singleton
class HomePriorityEngine @Inject constructor() {
    fun evaluate(
        offer: RideOffer,
        homeZone: HomeZone?,
        destLat: Double?,
        destLon: Double?,
        netProfit: Double?
    ): HomePriorityScore {
        if (homeZone == null) {
            return HomePriorityScore(
                score = 0,
                matchLevel = HomeMatchLevel.UNKNOWN,
                distanceResult = HomeDistanceResult(null, displayLabel = "UNKNOWN"),
                explanation = "HOME location not configured"
            )
        }

        val distanceKm = HomeDistanceCalculator.calculateHaversineKm(
            homeZone.latitude,
            homeZone.longitude,
            destLat,
            destLon
        )
        val distanceResult = HomeDistanceCalculator.formatDistance(distanceKm)
        if (distanceKm == null) {
            return HomePriorityScore(
                score = 0,
                matchLevel = HomeMatchLevel.UNKNOWN,
                distanceResult = distanceResult,
                explanation = "Destination coordinates unavailable"
            )
        }

        // Configurable-style deterministic weights:
        // Destination proximity: 40, Pickup efficiency: 20, Fare quality: 15, Profit: 15, Route: 10
        val proximityPts = when {
            distanceKm <= homeZone.radiusKm * 0.5 -> 40
            distanceKm <= homeZone.radiusKm -> 30
            distanceKm <= homeZone.radiusKm * 1.5 -> 18
            else -> 5
        }
        val pickupPts = when {
            offer.pickupDistanceKm == null -> 10
            offer.pickupDistanceKm <= 1.5 -> 20
            offer.pickupDistanceKm <= 3.0 -> 12
            else -> 4
        }
        val farePts = when {
            offer.fare == null -> 5
            offer.fare >= 150.0 -> 15
            offer.fare >= 80.0 -> 10
            else -> 4
        }
        val profitPts = when {
            netProfit == null -> 7
            netProfit >= 100.0 -> 15
            netProfit >= 50.0 -> 10
            else -> 3
        }
        val routePts = if (distanceKm <= homeZone.radiusKm) 10 else 4

        val total = (proximityPts + pickupPts + farePts + profitPts + routePts).coerceIn(0, 100)
        val matchLevel = when {
            total >= 75 -> HomeMatchLevel.HIGH
            total >= 50 -> HomeMatchLevel.MEDIUM
            else -> HomeMatchLevel.LOW
        }

        return HomePriorityScore(
            score = total,
            matchLevel = matchLevel,
            distanceResult = distanceResult,
            explanation = "Home match $matchLevel (${distanceResult.displayLabel})"
        )
    }
}
