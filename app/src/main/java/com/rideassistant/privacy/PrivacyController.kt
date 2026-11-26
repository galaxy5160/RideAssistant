package com.rideassistant.privacy

import com.rideassistant.data.database.dao.HomeLocationDao
import com.rideassistant.data.database.dao.RideHistoryDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyController @Inject constructor(
    private val rideHistoryDao: RideHistoryDao,
    private val homeLocationDao: HomeLocationDao
) {
    suspend fun clearAllRideHistory() {
        rideHistoryDao.deleteAll()
    }

    suspend fun clearHomeLocation() {
        homeLocationDao.clearHome()
    }
}
