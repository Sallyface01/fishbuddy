package com.fishbuddy.app

import android.app.Application
import com.amap.api.maps.MapsInitializer
import com.fishbuddy.app.data.local.AppDatabase
import com.fishbuddy.app.data.repository.AnalysisRepository
import com.fishbuddy.app.data.repository.CatchLogRepository
import com.fishbuddy.app.data.repository.SpotRepository
import com.fishbuddy.app.service.FishDatabaseService
import com.fishbuddy.app.service.FishingMethodsService

class FishBuddyApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    val analysisRepository by lazy { AnalysisRepository(database) }
    val spotRepository by lazy { SpotRepository(database) }
    val catchLogRepository by lazy { CatchLogRepository(database) }

    val fishDatabase by lazy { FishDatabaseService(this) }
    val fishingMethods by lazy { FishingMethodsService(this) }

    override fun onCreate() {
        super.onCreate()
        // 高德地图隐私合规（必须在地图初始化之前调用）
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
    }
}
