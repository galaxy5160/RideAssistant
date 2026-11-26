package com.rideassistant.capture

sealed interface CaptureEvent {
    data class OfferTextCaptured(
        val offerText: RideOfferText,
        val captureDurationMs: Long
    ) : CaptureEvent

    data class ServiceConnected(val timestamp: Long) : CaptureEvent
    data class ServiceDisconnected(val reason: String) : CaptureEvent
    data class CaptureError(val message: String) : CaptureEvent
}
