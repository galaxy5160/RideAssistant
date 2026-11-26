package com.rideassistant.rules

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AssistantRules(
    val isEnabled: Boolean = false,
    val autoAccept: Boolean = false,
    val minFare: Int = 50,
    val maxPickupKm: Double = 3.0,
    val minPerKmRate: Double = 10.0
)

data class RideLog(
    val timestamp: Long,
    val sourceApp: String,
    val fare: Int?,
    val distanceKm: Double?,
    val matched: Boolean,
    val actionTaken: String
)

object RulesEngine {
    private const val PREFS = "ride_assistant_prefs"
    private val _rules = MutableStateFlow(AssistantRules())
    val rules: StateFlow<AssistantRules> = _rules.asStateFlow()

    private val _logs = MutableStateFlow<List<RideLog>>(emptyList())
    val logs: StateFlow<List<RideLog>> = _logs.asStateFlow()

    fun init(context: Context) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _rules.value = AssistantRules(
            isEnabled = sp.getBoolean("is_enabled", false),
            autoAccept = sp.getBoolean("auto_accept", false),
            minFare = sp.getInt("min_fare", 50),
            maxPickupKm = sp.getFloat("max_pickup_km", 3.0f).toDouble(),
            minPerKmRate = sp.getFloat("min_per_km", 10.0f).toDouble()
        )
    }

    fun updateRules(context: Context, updated: AssistantRules) {
        _rules.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("is_enabled", updated.isEnabled)
            .putBoolean("auto_accept", updated.autoAccept)
            .putInt("min_fare", updated.minFare)
            .putFloat("max_pickup_km", updated.maxPickupKm.toFloat())
            .putFloat("min_per_km", updated.minPerKmRate.toFloat())
            .apply()
    }

    fun evaluate(fare: Int?, distanceKm: Double?): Boolean {
        val r = _rules.value
        if (!r.isEnabled) return false
        if (fare != null && fare < r.minFare) return false
        if (distanceKm != null && distanceKm > r.maxPickupKm) return false
        if (fare != null && distanceKm != null && distanceKm > 0.0) {
            if ((fare / distanceKm) < r.minPerKmRate) return false
        }
        return fare != null
    }

    fun addLog(log: RideLog) {
        _logs.value = (listOf(log) + _logs.value).take(50)
    }
}
