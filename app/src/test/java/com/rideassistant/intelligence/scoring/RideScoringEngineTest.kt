package com.rideassistant.intelligence.scoring

import com.rideassistant.intelligence.profit.ExpenseProfile
import com.rideassistant.intelligence.profit.ProfitCalculator
import com.rideassistant.parse.RideOffer
import org.junit.Assert.*
import org.junit.Test

class RideScoringEngineTest {

    private val profitCalc = ProfitCalculator()
    private val scoringEngine = RideScoringEngine()

    @Test
    fun `scores high-fare short-pickup ride as GOOD`() {
        val offer = RideOffer(
            id = "1",
            platform = "Rapido",
            fare = 220.0,
            pickupText = "Sector 15",
            destinationText = "Cyber Hub",
            pickupDistanceKm = 1.0,
            rideDistanceKm = 10.0,
            rideDurationMinutes = 25,
            rideType = "Auto",
            timestamp = 1000L,
            confidence = 0.9f,
            rawRelevantTexts = emptyList()
        )
        val profit = profitCalc.calculate(offer, ExpenseProfile(fuelCostPerKm = 3.5))
        val score = scoringEngine.score(offer, profit, homeDistanceKm = 2.0)

        assertEquals(ScoreCategory.GOOD, score.category)
        assertTrue(score.totalScore >= 75)
        assertTrue(profit.estimatedNetProfit!! > 100.0)
    }

    @Test
    fun `returns UNKNOWN when fare and ride distance are missing`() {
        val offer = RideOffer(
            id = "2",
            platform = "Ola",
            fare = null,
            pickupText = null,
            destinationText = null,
            pickupDistanceKm = null,
            rideDistanceKm = null,
            rideDurationMinutes = null,
            rideType = null,
            timestamp = 1000L,
            confidence = 0.0f,
            rawRelevantTexts = emptyList()
        )
        val profit = profitCalc.calculate(offer, ExpenseProfile())
        val score = scoringEngine.score(offer, profit, homeDistanceKm = null)

        assertEquals(ScoreCategory.UNKNOWN, score.category)
    }
}
