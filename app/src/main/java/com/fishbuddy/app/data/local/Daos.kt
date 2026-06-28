package com.fishbuddy.app.data.local

import androidx.room.*
import com.fishbuddy.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisRecordDao {
    @Insert
    suspend fun insert(record: AnalysisRecordEntity)

    @Query("SELECT * FROM analysis_records ORDER BY created_at DESC")
    fun getAll(): Flow<List<AnalysisRecordEntity>>

    @Query("SELECT * FROM analysis_records WHERE id = :id")
    suspend fun getById(id: String): AnalysisRecordEntity?

    @Delete
    suspend fun delete(record: AnalysisRecordEntity)

    @Query("DELETE FROM analysis_records")
    suspend fun deleteAll()
}

@Dao
interface FishingSpotDao {
    @Insert
    suspend fun insert(spot: FishingSpotEntity)

    @Query("SELECT * FROM fishing_spots ORDER BY created_at DESC")
    fun getAll(): Flow<List<FishingSpotEntity>>

    @Delete
    suspend fun delete(spot: FishingSpotEntity)
}

@Dao
interface CatchLogDao {
    @Insert
    suspend fun insert(log: CatchLogEntity)

    @Query("SELECT * FROM catch_logs WHERE species_name = :speciesName ORDER BY date DESC")
    fun getForSpecies(speciesName: String): Flow<List<CatchLogEntity>>

    @Delete
    suspend fun delete(log: CatchLogEntity)
}
