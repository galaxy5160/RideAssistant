package com.rideassistant.parse

sealed interface ParseResult {
    data class Success(
        val offer: RideOffer,
        val confidenceLevel: ConfidenceLevel,
        val parseTimeMs: Long
    ) : ParseResult

    data class Unknown(
        val platform: String,
        val reason: String,
        val parseTimeMs: Long
    ) : ParseResult

    data class Failure(
        val platform: String,
        val reason: String,
        val parseTimeMs: Long
    ) : ParseResult
}
