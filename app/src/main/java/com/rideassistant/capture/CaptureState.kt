package com.rideassistant.capture

enum class CaptureStatus {
    DISABLED,
    IDLE,
    ACTIVE,
    PAUSED,
    ERROR
}

data class CaptureState(
    val status: CaptureStatus = CaptureStatus.DISABLED,
    val activePlatform: String? = null,
    val lastCaptureTimestamp: Long = 0L,
    val totalEventsProcessed: Long = 0L,
    val lastError: String? = null
)
