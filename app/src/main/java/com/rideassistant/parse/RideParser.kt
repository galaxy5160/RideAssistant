package com.rideassistant.parse

import com.rideassistant.capture.RideOfferText
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideParser @Inject constructor(
    private val patternStore: PatternStore
) {
    fun parse(offerText: RideOfferText): ParseResult {
        val startNs = System.nanoTime()
        if (offerText.normalizedTexts.isEmpty()) {
            return ParseResult.Unknown(
                platform = offerText.platform,
                reason = "No visible text extracted",
                parseTimeMs = elapsedMs(startNs)
            )
        }

        val patterns = patternStore.getPatterns(offerText.platform)
        val joined = offerText.normalizedTexts.joinToString(" | ")

        val fare = patterns.fareRegex.find(joined)?.groupValues?.getOrNull(1)?.toDoubleOrNull()

        val distancesKm = mutableListOf<Double>()
        patterns.distanceKmRegex.findAll(joined).forEach { match ->
            match.groupValues.getOrNull(1)?.toDoubleOrNull()?.let { distancesKm.add(it) }
        }
        patterns.distanceMetersRegex.findAll(joined).forEach { match ->
            match.groupValues.getOrNull(1)?.toDoubleOrNull()?.let {
                distancesKm.add(RideNormalizer.roundToTwoDecimals(RideNormalizer.metersToKm(it)))
            }
        }

        val pickupDistanceKm = distancesKm.getOrNull(0)
        val rideDistanceKm = distancesKm.getOrNull(1)

        val durationMin = patterns.durationMinRegex.find(joined)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val rideType = patterns.rideTypeRegex.find(joined)?.groupValues?.getOrNull(1)

        val locationCandidates = offerText.normalizedTexts.filter { text ->
            text.length >= 6 &&
                !text.contains("₹") &&
                !patterns.distanceKmRegex.containsMatchIn(text) &&
                !patterns.durationMinRegex.containsMatchIn(text) &&
                !text.equals("Accept", ignoreCase = true) &&
                !text.equals("Decline", ignoreCase = true)
        }

        val pickupText = RideNormalizer.cleanLocationText(locationCandidates.getOrNull(0))
        val destinationText = RideNormalizer.cleanLocationText(locationCandidates.getOrNull(1))

        if (fare == null && pickupDistanceKm == null && rideDistanceKm == null) {
            return ParseResult.Unknown(
                platform = offerText.platform,
                reason = "Neither fare nor distance could be parsed",
                parseTimeMs = elapsedMs(startNs)
            )
        }

        var score = 0f
        if (fare != null) score += 0.35f
        if (rideDistanceKm != null) score += 0.25f
        if (pickupDistanceKm != null) score += 0.15f
        if (pickupText != null) score += 0.125f
        if (destinationText != null) score += 0.125f

        val confidenceLevel = when {
            score >= 0.75f -> ConfidenceLevel.HIGH
            score >= 0.45f -> ConfidenceLevel.MEDIUM
            score > 0f -> ConfidenceLevel.LOW
            else -> ConfidenceLevel.UNKNOWN
        }

        val offer = RideOffer(
            id = UUID.randomUUID().toString(),
            platform = offerText.platform,
            fare = fare,
            pickupText = pickupText,
            destinationText = destinationText,
            pickupDistanceKm = pickupDistanceKm,
            rideDistanceKm = rideDistanceKm,
            rideDurationMinutes = durationMin,
            rideType = rideType,
            timestamp = offerText.detectedAt,
            confidence = score,
            rawRelevantTexts = offerText.normalizedTexts.take(12)
        )

        return ParseResult.Success(
            offer = offer,
            confidenceLevel = confidenceLevel,
            parseTimeMs = elapsedMs(startNs)
        )
    }

    private fun elapsedMs(startNs: Long): Long = (System.nanoTime() - startNs) / 1_000_000L
}
