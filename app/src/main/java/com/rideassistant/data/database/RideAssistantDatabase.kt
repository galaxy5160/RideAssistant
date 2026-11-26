package com.rideassistant.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rideassistant.data.database.dao.*
import com.rideassistant.data.database.entities.*

@Database(
    entities = [
        RideHistoryEntity::class,
        RuleEntity::class,
        HomeLocationEntity::class,
        ExpenseProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RideAssistantDatabase : RoomDatabase() {
    abstract fun rideHistoryDao(): RideHistoryDao
    abstract fun ruleDao(): RuleDao
    abstract fun homeLocationDao(): HomeLocationDao
}
