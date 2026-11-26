package com.rideassistant.rules

import com.rideassistant.intelligence.profit.ProfitEstimate
import com.rideassistant.parse.RideOffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEngine @Inject constructor() {
    fun evaluate(
        offer: RideOffer,
        profit: ProfitEstimate,
        homeDistanceKm: Double?,
        rules: List<DriverRule>
    ): RuleEvaluationResult {
        val activeRules = rules.filter { it.enabled }.sortedByDescending { it.priority }
        val failed = mutableListOf<DriverRule>()
        val preferred = mutableListOf<DriverRule>()

        for (rule in activeRules) {
            when (rule.condition) {
                RuleCondition.MIN_FARE -> {
                    val minFare = rule.numericValue ?: continue
                    if (offer.fare != null && offer.fare < minFare) failed.add(rule)
                }
                RuleCondition.MIN_FARE_PER_KM -> {
                    val minPerKm = rule.numericValue ?: continue
                    if (offer.fare != null && offer.rideDistanceKm != null && offer.rideDistanceKm > 0) {
                        if ((offer.fare / offer.rideDistanceKm) < minPerKm) failed.add(rule)
                    }
                }
                RuleCondition.MIN_NET_PROFIT -> {
                    val minProfit = rule.numericValue ?: continue
                    if (profit.estimatedNetProfit != null && profit.estimatedNetProfit < minProfit) {
                        failed.add(rule)
                    }
                }
                RuleCondition.MAX_PICKUP_KM -> {
                    val maxPickup = rule.numericValue ?: continue
                    if (offer.pickupDistanceKm != null && offer.pickupDistanceKm > maxPickup) {
                        failed.add(rule)
                    }
                }
                RuleCondition.BLOCKED_DESTINATION -> {
                    val blocked = rule.textValue?.lowercase()?.trim() ?: continue
                    if (blocked.isNotEmpty() && offer.destinationText?.lowercase()?.contains(blocked) == true) {
                        failed.add(rule)
                    }
                }
                RuleCondition.PREFERRED_DESTINATION -> {
                    val pref = rule.textValue?.lowercase()?.trim() ?: continue
                    if (pref.isNotEmpty() && offer.destinationText?.lowercase()?.contains(pref) == true) {
                        preferred.add(rule)
                    }
                }
                RuleCondition.MAX_HOME_DISTANCE_KM -> {
                    val maxHome = rule.numericValue ?: continue
                    if (homeDistanceKm != null && homeDistanceKm > maxHome) {
                        failed.add(rule)
                    }
                }
            }
        }

        return RuleEvaluationResult(
            passedAll = failed.isEmpty(),
            failedRules = failed,
            matchedPreferredRules = preferred
        )
    }
}
