package com.rideassistant.intelligence.scoring

import com.rideassistant.intelligence.profit.ProfitEstimate
import com.rideassistant.parse.RideOffer
import javax.inject.Inject
import javax.inject.Singleton

enum class ScoreCategory {
    GOOD,
    CHECK,
    LOW,
    UNKNOWN
}

data class ScoringWeights(
    val fareQuality: Int = 25,
    val pickupEfficiency: Int = 20,
    val profitEstimate: Int = 20,
    val destinationPreference: Int = 15,
    val homeProximity: Int = 15,
    val rideCompatibility: Int = 5
)

data class ScoreBreakdown(
    val fareScore: Int,
    val pickupScore: Int,
    val profitScore: Int,
    val destinationScore: Int,
    val homeScore: Int,
    val compatibilityScore: Int
)

data class RideScore(
    val totalScore: Int,
    val category: ScoreCategory,
    val breakdown: ScoreBreakdown,
    val explanation: String
)

@Singleton
class RideScoringEngine @Inject constructor() {
    fun score(
        offer: RideOffer,
        profit: ProfitEstimate,
        homeDistanceKm: Double?,
        weights: ScoringWeights = ScoringWeights()
    ): RideScore {
        if (offer.fare == null && offer.rideDistanceKm == null) {
            return RideScore(
                totalScore = 0,
                category = ScoreCategory.UNKNOWN,
                breakdown = ScoreBreakdown(0, 0, 0, 0, 0, 0),
                explanation = "Insufficient offer data to score"
            )
        }

        val farePerKm = if (offer.fare != null && offer.rideDistanceKm != null && offer.rideDistanceKm > 0) {
            offer.fare / offer.rideDistanceKm
        } else null

        val fareScore = when {
            farePerKm == null -> weights.fareQuality / 2
            farePerKm >= 18.0 -> weights.fareQuality
            farePerKm >= 12.0 -> (weights.fareQuality * 0.7).toInt()
            else -> (weights.fareQuality * 0.3).toInt()
        }

        val pickupScore = when {
            offer.pickupDistanceKm == null -> weights.pickupEfficiency / 2
            offer.pickupDistanceKm <= 1.5 -> weights.pickupEfficiency
            offer.pickupDistanceKm <= 3.0 -> (weights.pickupEfficiency * 0.65).toInt()
            else -> (weights.pickupEfficiency * 0.25).toInt()
        }

        val profitScore = when {
            profit.estimatedNetProfit == null -> weights.profitEstimate / 2
            profit.estimatedNetProfit >= 100.0 -> weights.profitEstimate
            profit.estimatedNetProfit >= 40.0 -> (weights.profitEstimate * 0.65).toInt()
            else -> (weights.profitEstimate * 0.2).toInt()
        }

        val destinationScore = if (offer.destinationText != null) weights.destinationPreference else weights.destinationPreference / 2

        val homeScore = when {
            homeDistanceKm == null -> weights.homeProximity / 2
            homeDistanceKm <= 3.0 -> weights.homeProximity
            homeDistanceKm <= 7.0 -> (weights.homeProximity * 0.6).toInt()
            else -> (weights.homeProximity * 0.2).toInt()
        }

        val compatibilityScore = weights.rideCompatibility

        val total = (fareScore + pickupScore + profitScore + destinationScore + homeScore + compatibilityScore)
            .coerceIn(0, 100)

        val category = when {
            total >= 75 -> ScoreCategory.GOOD
            total >= 50 -> ScoreCategory.CHECK
            else -> ScoreCategory.LOW
        }

        val explanation = buildString {
            append("Score $total/100 ($category)")
            farePerKm?.let { append(" • ₹${String.format("%.1f", it)}/km") }
            profit.estimatedNetProfit?.let { append(" • Est. Net ₹${it.toInt()}") }
        }

        return RideScore(
            totalScore = total,
            category = category,
            breakdown = ScoreBreakdown(
                fareScore = fareScore,
                pickupScore = pickupScore,
                profitScore = profitScore,
                destinationScore = destinationScore,
                homeScore = homeScore,
                compatibilityScore = compatibilityScore
            ),
            explanation = explanation
        )
    }
}
