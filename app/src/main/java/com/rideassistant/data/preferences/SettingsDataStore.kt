package com.rideassistant.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ride_assistant_prefs")

data class UserSettings(
    val onboardingCompleted: Boolean = false,
    val monitoringEnabled: Boolean = false,
    val homePriorityEnabled: Boolean = false,
    val bubbleEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val voiceEnabled: Boolean = false,
    val minFareRule: Double = 40.0,
    val maxPickupKmRule: Double = 3.5,
    val minFarePerKmRule: Double = 10.0,
    val fuelCostPerKm: Double = 3.5
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
        val HOME_PRIORITY_ENABLED = booleanPreferencesKey("home_priority_enabled")
        val BUBBLE_ENABLED = booleanPreferencesKey("bubble_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val MIN_FARE = doublePreferencesKey("min_fare_rule")
        val MAX_PICKUP_KM = doublePreferencesKey("max_pickup_km_rule")
        val MIN_FARE_PER_KM = doublePreferencesKey("min_fare_per_km_rule")
        val FUEL_COST_PER_KM = doublePreferencesKey("fuel_cost_per_km")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            monitoringEnabled = prefs[Keys.MONITORING_ENABLED] ?: false,
            homePriorityEnabled = prefs[Keys.HOME_PRIORITY_ENABLED] ?: false,
            bubbleEnabled = prefs[Keys.BUBBLE_ENABLED] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            voiceEnabled = prefs[Keys.VOICE_ENABLED] ?: false,
            minFareRule = prefs[Keys.MIN_FARE] ?: 40.0,
            maxPickupKmRule = prefs[Keys.MAX_PICKUP_KM] ?: 3.5,
            minFarePerKmRule = prefs[Keys.MIN_FARE_PER_KM] ?: 10.0,
            fuelCostPerKm = prefs[Keys.FUEL_COST_PER_KM] ?: 3.5
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MONITORING_ENABLED] = enabled }
    }

    suspend fun setHomePriorityEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HOME_PRIORITY_ENABLED] = enabled }
    }

    suspend fun setBubbleEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BUBBLE_ENABLED] = enabled }
    }

    suspend fun updateRules(minFare: Double, maxPickupKm: Double, minFarePerKm: Double, fuelCostPerKm: Double) {
        context.dataStore.edit {
            it[Keys.MIN_FARE] = minFare
            it[Keys.MAX_PICKUP_KM] = maxPickupKm
            it[Keys.MIN_FARE_PER_KM] = minFarePerKm
            it[Keys.FUEL_COST_PER_KM] = fuelCostPerKm
        }
    }
}
