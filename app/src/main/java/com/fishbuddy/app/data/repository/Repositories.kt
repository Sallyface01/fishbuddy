package com.fishbuddy.app.data.repository

import com.fishbuddy.app.data.local.AppDatabase
import com.fishbuddy.app.data.model.*
import kotlinx.coroutines.flow.Flow

class AnalysisRepository(private val db: AppDatabase) {
    private val dao = db.analysisRecordDao()
    fun getAll(): Flow<List<AnalysisRecordEntity>> = dao.getAll()
    suspend fun insert(record: AnalysisRecordEntity) = dao.insert(record)
    suspend fun getById(id: String) = dao.getById(id)
    suspend fun delete(record: AnalysisRecordEntity) = dao.delete(record)
    suspend fun deleteAll() = dao.deleteAll()
}

class SpotRepository(private val db: AppDatabase) {
    private val dao = db.fishingSpotDao()
    fun getAll(): Flow<List<FishingSpotEntity>> = dao.getAll()
    suspend fun insert(spot: FishingSpotEntity) = dao.insert(spot)
    suspend fun delete(spot: FishingSpotEntity) = dao.delete(spot)
}

class CatchLogRepository(private val db: AppDatabase) {
    private val dao = db.catchLogDao()
    fun getForSpecies(speciesName: String): Flow<List<CatchLogEntity>> = dao.getForSpecies(speciesName)
    suspend fun insert(log: CatchLogEntity) = dao.insert(log)
    suspend fun delete(log: CatchLogEntity) = dao.delete(log)
}
