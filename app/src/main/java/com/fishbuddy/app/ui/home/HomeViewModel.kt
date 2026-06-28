package com.fishbuddy.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fishbuddy.app.FishBuddyApp
import com.fishbuddy.app.data.model.AnalysisRecordEntity
import com.fishbuddy.app.domain.model.FishSpecies
import com.fishbuddy.app.domain.model.FishingRecommendation
import com.fishbuddy.app.domain.model.WaterBodyType
import com.fishbuddy.app.domain.model.WeatherData
import com.fishbuddy.app.service.CameraService
import com.fishbuddy.app.service.LocationService
import com.fishbuddy.app.service.WaterClassifierService
import com.fishbuddy.app.service.WeatherService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val appCtx = app as FishBuddyApp
    private val db = appCtx.fishDatabase
    private val classifier = WaterClassifierService()
    private val locationService = LocationService(app)
    private val weatherService = WeatherService()

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private val gson = Gson()

    fun showCamera() { _state.value = _state.value.copy(showCameraOptions = true) }
    fun dismissCameraOptions() { _state.value = _state.value.copy(showCameraOptions = false) }

    fun onImageCaptured(imageData: ByteArray) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isAnalyzing = true, loadingMessage = "正在压缩图片...")

            // Step 1: Compress
            val compressed = CameraService.compressImage(imageData)
            val thumbnail = CameraService.makeThumbnail(compressed)

            _state.value = _state.value.copy(loadingMessage = "正在获取位置...")
            // Step 2: Location
            val location = withContext(Dispatchers.IO) { locationService.fetchCurrentLocation() }

            // Step 2b: Weather (non-fatal)
            var weather: WeatherData? = null
            if (location != null) {
                weather = weatherService.fetchWeather(location.latitude, location.longitude)
            }

            _state.value = _state.value.copy(loadingMessage = "正在识别水域类型...")
            // Step 3: Water classification
            val (waterType, confidence) = withContext(Dispatchers.IO) {
                classifier.classify(compressed)
            }

            _state.value = _state.value.copy(loadingMessage = "正在匹配当地鱼种...")
            // Step 4: Query fish database
            val (species, recommendations) = withContext(Dispatchers.IO) {
                db.query(waterType.chineseName, location)
            }

            // Step 5: Build conditions text
            val conditions = when {
                confidence > 0.5f -> "${waterType.chineseName}，识别置信度 ${(confidence * 100).toInt()}%"
                confidence > 0f -> "${waterType.chineseName}，置信度较低"
                else -> "自动识别未成功，默认为湖泊类型"
            }

            // Step 6: Create and save record
            val fishJSON = gson.toJson(species)
            val recJSON = gson.toJson(recommendations)
            val locationName = db.resolveLocationName(location)

            val weatherJSON = weather?.let { gson.toJson(it) }

            val record = AnalysisRecordEntity(
                photoData = compressed,
                thumbnailData = thumbnail,
                latitude = location?.latitude,
                longitude = location?.longitude,
                locationName = locationName ?: location?.name,
                waterBodyType = waterType.chineseName,
                waterConditions = conditions,
                fishSpeciesJSON = fishJSON,
                recommendationsJSON = recJSON,
                weatherJSON = weatherJSON
            )

            withContext(Dispatchers.IO) { appCtx.analysisRepository.insert(record) }

            _state.value = _state.value.copy(
                isAnalyzing = false,
                analysisResult = record,
                resultSpecies = species,
                resultRecommendations = recommendations,
                resultWaterType = waterType
            )
        }
    }

    fun reset() { _state.value = HomeState() }
}

data class HomeState(
    val showCameraOptions: Boolean = false,
    val isAnalyzing: Boolean = false,
    val loadingMessage: String = "正在分析水域...",
    val analysisResult: AnalysisRecordEntity? = null,
    val resultSpecies: List<FishSpecies> = emptyList(),
    val resultRecommendations: List<FishingRecommendation> = emptyList(),
    val resultWaterType: WaterBodyType = WaterBodyType.LAKE
)
