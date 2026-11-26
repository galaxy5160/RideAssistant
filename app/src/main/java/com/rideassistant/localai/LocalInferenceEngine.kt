package com.rideassistant.localai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional local inference engine (e.g. llama.cpp GGUF).
 * Never required for core features, never uses cloud APIs, never auto-downloads models.
 */
@Singleton
class LocalInferenceEngine @Inject constructor() {
    private var modelLoaded = false

    fun isModelLoaded(): Boolean = modelLoaded

    fun explainScoreLocally(score: Int, category: String): String {
        return "Deterministic breakdown: Score $score/100 ($category). Calculated offline from fare/km, pickup distance, estimated net profit, and Home proximity."
    }
}
