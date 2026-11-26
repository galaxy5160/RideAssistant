package com.rideassistant.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_history")
data class RideHistoryEntity(
    @PrimaryKey val id: String,
    val platform: String,
    val timestamp: Long,
    val fare: Double?,
    val pickupText: String?,
    val destinationText: String?,
    val pickupDistanceKm: Double?,
    val rideDistanceKm: Double?,
    val homeDistanceKm: Double?,
    val distanceType: String,
    val score: Int,
    val category: String,
    val parserConfidence: Float,
    val estimatedExpenses: Double,
    val estimatedProfit: Double?,
    val isHomePriorityMatch: Boolean
)

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val condition: String,
    val numericValue: Double?,
    val textValue: String?,
    val enabled: Boolean,
    val priority: Int,
    val explanation: String
)

@Entity(tableName = "home_locations")
data class HomeLocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double,
    val updatedAt: Long
)

@Entity(tableName = "expense_profiles")
data class ExpenseProfileEntity(
    @PrimaryKey val id: String,
    val fuelType: String,
    val fuelCostPerKm: Double,
    val maintenanceCostPerKm: Double,
    val tyreReservePerKm: Double,
    val defaultToll: Double,
    val defaultParking: Double
)
