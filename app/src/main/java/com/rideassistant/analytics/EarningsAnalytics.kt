package com.rideassistant.analytics

import com.rideassistant.data.database.entities.RideHistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

data class RideStatisticsSummary(
    val rideCount: Int,
    val grossEarnings: Double,
    val estimatedNetProfit: Double,
    val averageFare: Double,
    val totalRideKm: Double,
    val totalPickupKm: Double,
    val homePriorityMatches: Int,
    val platformCounts: Map<String, Int>
)

@Singleton
class EarningsAnalytics @Inject constructor() {
    fun summarize(rides: List<RideHistoryEntity>): RideStatisticsSummary {
        if (rides.isEmpty()) {
            return RideStatisticsSummary(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, emptyMap())
        }
        val gross = rides.sumOf { it.fare ?: 0.0 }
        val net = rides.sumOf { it.estimatedProfit ?: 0.0 }
        val rideKm = rides.sumOf { it.rideDistanceKm ?: 0.0 }
        val pickupKm = rides.sumOf { it.pickupDistanceKm ?: 0.0 }
        val homeMatches = rides.count { it.isHomePriorityMatch }
        val platforms = rides.groupingBy { it.platform }.eachCount()

        return RideStatisticsSummary(
            rideCount = rides.size,
            grossEarnings = gross,
            estimatedNetProfit = net,
            averageFare = gross / rides.size,
            totalRideKm = rideKm,
            totalPickupKm = pickupKm,
            homePriorityMatches = homeMatches,
            platformCounts = platforms
        )
    }
}
