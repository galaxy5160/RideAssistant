package com.rideassistant.data.database.dao

import androidx.room.*
import com.rideassistant.data.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RideHistoryDao {
    @Query("SELECT * FROM ride_history ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<RideHistoryEntity>>

    @Query("SELECT * FROM ride_history WHERE timestamp >= :sinceMs ORDER BY timestamp DESC")
    fun observeSince(sinceMs: Long): Flow<List<RideHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ride: RideHistoryEntity)

    @Query("DELETE FROM ride_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM ride_history")
    suspend fun deleteAll()
}

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY priority DESC")
    fun observeAll(): Flow<List<RuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: RuleEntity)

    @Query("DELETE FROM rules WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface HomeLocationDao {
    @Query("SELECT * FROM home_locations LIMIT 1")
    fun observeHome(): Flow<HomeLocationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHome(home: HomeLocationEntity)

    @Query("DELETE FROM home_locations")
    suspend fun clearHome()
}
