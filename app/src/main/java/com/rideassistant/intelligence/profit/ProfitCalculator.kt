package com.rideassistant.intelligence.profit

import com.rideassistant.parse.RideOffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

enum class FuelType {
    PETROL,
    DIESEL,
    CNG,
    ELECTRIC,
    OTHER
}

data class ExpenseProfile(
    val fuelType: FuelType = FuelType.CNG,
    val fuelCostPerKm: Double = 3.5,
    val maintenanceCostPerKm: Double = 1.0,
    val tyreReservePerKm: Double = 0.5,
    val defaultToll: Double = 0.0,
    val defaultParking: Double = 0.0,
    val otherExpense: Double = 0.0
) {
    val totalPerKm: Double get() = fuelCostPerKm + maintenanceCostPerKm + tyreReservePerKm
}

data class ProfitEstimate(
    val grossFare: Double?,
    val pickupTravelExpense: Double,
    val rideTravelExpense: Double,
    val totalEstimatedExpense: Double,
    val estimatedNetProfit: Double?,
    val estimatedProfitPerHour: Double?,
    val isEstimate: Boolean = true
)

@Singleton
class ProfitCalculator @Inject constructor() {
    fun calculate(offer: RideOffer, profile: ExpenseProfile): ProfitEstimate {
        val pickupKm = offer.pickupDistanceKm ?: 0.0
        val rideKm = offer.rideDistanceKm ?: 0.0
        val pickupExpense = round2(pickupKm * profile.totalPerKm)
        val rideExpense = round2(rideKm * profile.totalPerKm)
        val totalExpense = round2(
            pickupExpense + rideExpense + profile.defaultToll + profile.defaultParking + profile.otherExpense
        )

        val netProfit = offer.fare?.let { round2(it - totalExpense) }
        val profitPerHour = if (netProfit != null && offer.rideDurationMinutes != null && offer.rideDurationMinutes > 0) {
            round2(netProfit * (60.0 / offer.rideDurationMinutes))
        } else {
            null
        }

        return ProfitEstimate(
            grossFare = offer.fare,
            pickupTravelExpense = pickupExpense,
            rideTravelExpense = rideExpense,
            totalEstimatedExpense = totalExpense,
            estimatedNetProfit = netProfit,
            estimatedProfitPerHour = profitPerHour
        )
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
