package com.rideassistant.capture

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureManager @Inject constructor() {
    private val _state = MutableStateFlow(CaptureState())
    val state: StateFlow<CaptureState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CaptureEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<CaptureEvent> = _events.asSharedFlow()

    private var lastSignature: Int = 0
    private var lastSignatureTime: Long = 0L

    fun setPaused(paused: Boolean) {
        _state.update {
            it.copy(status = if (paused) CaptureStatus.PAUSED else CaptureStatus.IDLE)
        }
    }

    fun onServiceConnected() {
        _state.update { it.copy(status = CaptureStatus.IDLE, lastError = null) }
        _events.tryEmit(CaptureEvent.ServiceConnected(System.currentTimeMillis()))
    }

    fun onServiceDisconnected(reason: String) {
        _state.update { it.copy(status = CaptureStatus.DISABLED, lastError = reason) }
        _events.tryEmit(CaptureEvent.ServiceDisconnected(reason))
    }

    fun onOfferCaptured(offerText: RideOfferText, captureDurationMs: Long): Boolean {
        if (_state.value.status == CaptureStatus.PAUSED) return false

        val sig = offerText.normalizedTexts.hashCode()
        val now = System.currentTimeMillis()
        if (sig == lastSignature && (now - lastSignatureTime) < 2500L) {
            return false // Duplicate suppression
        }
        lastSignature = sig
        lastSignatureTime = now

        _state.update {
            it.copy(
                status = CaptureStatus.ACTIVE,
                activePlatform = offerText.platform,
                lastCaptureTimestamp = now,
                totalEventsProcessed = it.totalEventsProcessed + 1,
                lastError = null
            )
        }
        _events.tryEmit(CaptureEvent.OfferTextCaptured(offerText, captureDurationMs))
        return true
    }

    fun onError(message: String) {
        _state.update { it.copy(status = CaptureStatus.ERROR, lastError = message) }
        _events.tryEmit(CaptureEvent.CaptureError(message))
    }
}
