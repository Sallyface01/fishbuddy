package com.fishbuddy.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fishbuddy.app.data.model.*

@Database(
    entities = [
        AnalysisRecordEntity::class,
        FishingSpotEntity::class,
        CatchLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisRecordDao(): AnalysisRecordDao
    abstract fun fishingSpotDao(): FishingSpotDao
    abstract fun catchLogDao(): CatchLogDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fishbuddy.db"
                ).build().also { instance = it }
            }
    }
}
