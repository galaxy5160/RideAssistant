package com.rideassistant.parse

import javax.inject.Inject
import javax.inject.Singleton

data class PlatformPatterns(
    val platform: String,
    val version: Int,
    val fareRegex: Regex,
    val distanceKmRegex: Regex,
    val distanceMetersRegex: Regex,
    val durationMinRegex: Regex,
    val rideTypeRegex: Regex
)

@Singleton
class PatternStore @Inject constructor() {
    private val defaultFare = Regex("₹\\s*([0-9]+(?:\\.[0-9]{1,2})?)")
    private val defaultKm = Regex("([0-9]+(?:\\.[0-9]{1,2})?)\\s*(?i)km\\b")
    private val defaultMeters = Regex("([0-9]+)\\s*(?i)m\\b")
    private val defaultMinutes = Regex("([0-9]+)\\s*(?i)(min|mins|minute|minutes)\\b")
    private val defaultRideType = Regex("(?i)\\b(Auto|Mini|Sedan|Prime|SUV|Bike|Moto|Parcel|Porter|Cab|Go|Premier)\\b")

    private val patterns: Map<String, PlatformPatterns> = listOf(
        "Ola", "Uber", "Rapido", "Porter", "inDrive"
    ).associateWith { platform ->
        PlatformPatterns(
            platform = platform,
            version = 1,
            fareRegex = defaultFare,
            distanceKmRegex = defaultKm,
            distanceMetersRegex = defaultMeters,
            durationMinRegex = defaultMinutes,
            rideTypeRegex = defaultRideType
        )
    }

    fun getPatterns(platform: String): PlatformPatterns =
        patterns[platform] ?: PlatformPatterns(
            platform = platform,
            version = 1,
            fareRegex = defaultFare,
            distanceKmRegex = defaultKm,
            distanceMetersRegex = defaultMeters,
            durationMinRegex = defaultMinutes,
            rideTypeRegex = defaultRideType
        )
}
