package com.rideassistant.parse

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional offline OCR fallback.
 * Never part of the critical path when AccessibilityService text extraction succeeds.
 * Returns honest unavailability when no offline OCR engine is bundled.
 */
@Singleton
class OCRFallback @Inject constructor() {
    fun isAvailable(): Boolean = false

    fun extractTextFallback(platform: String): ParseResult {
        return ParseResult.Failure(
            platform = platform,
            reason = "Offline OCR module not installed; using Accessibility text only.",
            parseTimeMs = 0L
        )
    }
}
