package com.rideassistant.action

import com.rideassistant.capture.CaptureEvent
import com.rideassistant.capture.CaptureManager
import com.rideassistant.data.database.dao.HomeLocationDao
import com.rideassistant.data.database.dao.RideHistoryDao
import com.rideassistant.data.database.entities.RideHistoryEntity
import com.rideassistant.data.preferences.SettingsDataStore
import com.rideassistant.data.preferences.UserSettings
import com.rideassistant.intelligence.home.HomeDistanceResult
import com.rideassistant.intelligence.home.HomeZone
import com.rideassistant.intelligence.profit.ExpenseProfile
import com.rideassistant.intelligence.profit.ProfitCalculator
import com.rideassistant.intelligence.scoring.RideScore
import com.rideassistant.intelligence.scoring.RideScoringEngine
import com.rideassistant.parse.ParseResult
import com.rideassistant.parse.RideOffer
import com.rideassistant.parse.RideParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class AnalyzedOfferState(
    val offer: RideOffer,
    val score: RideScore,
    val homeDistance: HomeDistanceResult,
    val netProfit: Double?,
    val pipelineLatencyMs: Long
)

@Singleton
class DriverAssistController @Inject constructor(
    private val captureManager: CaptureManager,
    private val parser: RideParser,
    private val profitCalculator: ProfitCalculator,
    private val scoringEngine: RideScoringEngine,
    private val notificationController: NotificationController,
    private val rideHistoryDao: RideHistoryDao,
    private val homeLocationDao: HomeLocationDao,
    private val settingsDataStore: SettingsDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _latestOffer = MutableStateFlow<AnalyzedOfferState?>(null)
    val latestOffer: StateFlow<AnalyzedOfferState?> = _latestOffer.asStateFlow()

    @Volatile private var cachedSettings = UserSettings()
    @Volatile private var cachedHomeZone: HomeZone? = null

    init {
        scope.launch {
            settingsDataStore.settingsFlow.collect { cachedSettings = it }
        }
        scope.launch {
            homeLocationDao.observeHome().collect { entity ->
                cachedHomeZone = entity?.let {
                    HomeZone(it.name, it.latitude, it.longitude, it.radiusKm)
                }
            }
        }
        scope.launch {
            captureManager.events.collect { event ->
                if (event is CaptureEvent.OfferTextCaptured && cachedSettings.monitoringEnabled) {
                    processCapturedOffer(event)
                }
            }
        }
    }

    private fun processCapturedOffer(event: CaptureEvent.OfferTextCaptured) {
        val startNs = System.nanoTime()
        val parseResult = parser.parse(event.offerText)
        if (parseResult !is ParseResult.Success) return

        val offer = parseResult.offer
        val profile = ExpenseProfile(fuelCostPerKm = cachedSettings.fuelCostPerKm)
        val profit = profitCalculator.calculate(offer, profile)
        val homeDist = HomeDistanceResult(
            distanceKm = null,
            displayLabel = if (cachedHomeZone == null) "HOME not set" else "UNKNOWN (No dest coords)"
        )
        val score = scoringEngine.score(offer, profit, homeDist.distanceKm)
        val totalMs = (System.nanoTime() - startNs) / 1_000_000L + event.captureDurationMs

        val analyzed = AnalyzedOfferState(
            offer = offer,
            score = score,
            homeDistance = homeDist,
            netProfit = profit.estimatedNetProfit,
            pipelineLatencyMs = totalMs
        )
        _latestOffer.value = analyzed

        if (cachedSettings.notificationsEnabled) {
            notificationController.showOfferAlert(
                platform = offer.platform,
                fare = offer.fare,
                pickupKm = offer.pickupDistanceKm,
                rideKm = offer.rideDistanceKm,
                homeDistanceLabel = homeDist.displayLabel,
                score = score.totalScore,
                category = score.category.name,
                isHomePriority = cachedSettings.homePriorityEnabled
            )
        }

        // Persist asynchronously without blocking alert dispatch
        scope.launch(Dispatchers.IO) {
            rideHistoryDao.insert(
                RideHistoryEntity(
                    id = offer.id,
                    platform = offer.platform,
                    timestamp = offer.timestamp,
                    fare = offer.fare,
                    pickupText = offer.pickupText,
                    destinationText = offer.destinationText,
                    pickupDistanceKm = offer.pickupDistanceKm,
                    rideDistanceKm = offer.rideDistanceKm,
                    homeDistanceKm = homeDist.distanceKm,
                    distanceType = homeDist.distanceType,
                    score = score.totalScore,
                    category = score.category.name,
                    parserConfidence = offer.confidence,
                    estimatedExpenses = profit.totalEstimatedExpense,
                    estimatedProfit = profit.estimatedNetProfit,
                    isHomePriorityMatch = cachedSettings.homePriorityEnabled
                )
            )
        }
    }
}
