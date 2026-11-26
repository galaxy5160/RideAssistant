package com.rideassistant.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rideassistant.action.AnalyzedOfferState
import com.rideassistant.action.DriverAssistController
import com.rideassistant.analytics.EarningsAnalytics
import com.rideassistant.analytics.RideStatisticsSummary
import com.rideassistant.capture.CaptureManager
import com.rideassistant.capture.CaptureState
import com.rideassistant.data.database.dao.HomeLocationDao
import com.rideassistant.data.database.dao.RideHistoryDao
import com.rideassistant.data.database.entities.HomeLocationEntity
import com.rideassistant.data.database.entities.RideHistoryEntity
import com.rideassistant.data.preferences.SettingsDataStore
import com.rideassistant.data.preferences.UserSettings
import com.rideassistant.diagnose.DiagnosticsManager
import com.rideassistant.diagnose.HealthCheck
import com.rideassistant.privacy.PrivacyController
import com.rideassistant.service.BubbleService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RideAssistantViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore,
    private val captureManager: CaptureManager,
    private val driverAssistController: DriverAssistController,
    private val rideHistoryDao: RideHistoryDao,
    private val homeLocationDao: HomeLocationDao,
    private val earningsAnalytics: EarningsAnalytics,
    private val diagnosticsManager: DiagnosticsManager,
    private val privacyController: PrivacyController
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val captureState: StateFlow<CaptureState> = captureManager.state

    val latestOffer: StateFlow<AnalyzedOfferState?> = driverAssistController.latestOffer

    val rideHistory: StateFlow<List<RideHistoryEntity>> = rideHistoryDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeLocation: StateFlow<HomeLocationEntity?> = homeLocationDao.observeHome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val analyticsSummary: StateFlow<RideStatisticsSummary> = rideHistory
        .map { earningsAnalytics.summarize(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RideStatisticsSummary(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, emptyMap())
        )

    private val _diagnostics = MutableStateFlow<List<HealthCheck>>(emptyList())
    val diagnostics: StateFlow<List<HealthCheck>> = _diagnostics.asStateFlow()

    fun refreshDiagnostics() {
        _diagnostics.value = diagnosticsManager.runChecks(homeLocation.value != null)
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsDataStore.setOnboardingCompleted(true) }
    }

    fun toggleMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setMonitoringEnabled(enabled)
            captureManager.setPaused(!enabled)
        }
    }

    fun toggleHomePriority(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.setHomePriorityEnabled(enabled) }
    }

    fun toggleBubble(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setBubbleEnabled(enabled)
            val intent = Intent(context, BubbleService::class.java)
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }
    }

    fun saveHomeLocation(name: String, lat: Double, lon: Double, radiusKm: Double = 5.0) {
        viewModelScope.launch {
            homeLocationDao.saveHome(
                HomeLocationEntity(
                    id = "primary_home",
                    name = name,
                    latitude = lat,
                    longitude = lon,
                    radiusKm = radiusKm,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveInitialMinFare(minFare: Double) {
        viewModelScope.launch {
            val s = settings.value
            settingsDataStore.updateRules(minFare, s.maxPickupKmRule, s.minFarePerKmRule, s.fuelCostPerKm)
        }
    }

    fun updateRules(minFare: Double, maxPickupKm: Double, minFarePerKm: Double, fuelCostPerKm: Double) {
        viewModelScope.launch {
            settingsDataStore.updateRules(minFare, maxPickupKm, minFarePerKm, fuelCostPerKm)
        }
    }

    fun deleteRide(id: String) {
        viewModelScope.launch { rideHistoryDao.deleteById(id) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { privacyController.clearAllRideHistory() }
    }

    fun clearHomeLocation() {
        viewModelScope.launch { privacyController.clearHomeLocation() }
    }
}
