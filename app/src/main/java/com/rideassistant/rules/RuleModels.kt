package com.rideassistant.rules

enum class RuleCategory {
    FARE,
    PICKUP,
    DESTINATION,
    RIDE,
    HOME
}

enum class RuleCondition {
    MIN_FARE,
    MIN_FARE_PER_KM,
    MIN_NET_PROFIT,
    MAX_PICKUP_KM,
    BLOCKED_DESTINATION,
    PREFERRED_DESTINATION,
    MAX_HOME_DISTANCE_KM
}

data class DriverRule(
    val id: String,
    val name: String,
    val category: RuleCategory,
    val condition: RuleCondition,
    val numericValue: Double? = null,
    val textValue: String? = null,
    val enabled: Boolean = true,
    val priority: Int = 1,
    val explanation: String = ""
)

data class RuleEvaluationResult(
    val passedAll: Boolean,
    val failedRules: List<DriverRule>,
    val matchedPreferredRules: List<DriverRule>
)
