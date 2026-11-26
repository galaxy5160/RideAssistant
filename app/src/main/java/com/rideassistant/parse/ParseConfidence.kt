package com.rideassistant.parse

enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN
}

data class RideOffer(
    val id: String,
    val platform: String,
    val fare: Double?,
    val pickupText: String?,
    val destinationText: String?,
    val pickupDistanceKm: Double?,
    val rideDistanceKm: Double?,
    val rideDurationMinutes: Int?,
    val rideType: String?,
    val timestamp: Long,
    val confidence: Float,
    val rawRelevantTexts: List<String>
)
