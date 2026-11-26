package com.rideassistant.di

import android.content.Context
import androidx.room.Room
import com.rideassistant.data.database.RideAssistantDatabase
import com.rideassistant.data.database.dao.HomeLocationDao
import com.rideassistant.data.database.dao.RideHistoryDao
import com.rideassistant.data.database.dao.RuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RideAssistantDatabase {
        return Room.databaseBuilder(
            context,
            RideAssistantDatabase::class.java,
            "ride_assistant.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideRideHistoryDao(db: RideAssistantDatabase): RideHistoryDao = db.rideHistoryDao()

    @Provides
    fun provideRuleDao(db: RideAssistantDatabase): RuleDao = db.ruleDao()

    @Provides
    fun provideHomeLocationDao(db: RideAssistantDatabase): HomeLocationDao = db.homeLocationDao()
}
