package com.fishbuddy.app.ui.spots

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fishbuddy.app.FishBuddyApp
import com.fishbuddy.app.data.model.FishingSpotEntity
import com.amap.api.maps.model.LatLng
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SpotsViewModel(app: Application) : AndroidViewModel(app) {
    private val appCtx = app as FishBuddyApp

    private val _spots = MutableStateFlow<List<FishingSpotEntity>>(emptyList())
    val spots: StateFlow<List<FishingSpotEntity>> = _spots

    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding

    private val _newCoord = MutableStateFlow<LatLng?>(null)
    val newCoord: StateFlow<LatLng?> = _newCoord

    private val _selectedSpot = MutableStateFlow<FishingSpotEntity?>(null)
    val selectedSpot: StateFlow<FishingSpotEntity?> = _selectedSpot

    init {
        viewModelScope.launch {
            appCtx.spotRepository.getAll().collect { _spots.value = it }
        }
    }

    fun addSpot(latLng: LatLng) {
        _newCoord.value = latLng
        _isAdding.value = true
    }

    fun saveSpot(name: String, waterType: String?, notes: String?) {
        val c = _newCoord.value ?: return
        viewModelScope.launch {
            appCtx.spotRepository.insert(FishingSpotEntity(
                name = name.ifEmpty { "未命名钓点" },
                latitude = c.latitude, longitude = c.longitude,
                waterBodyType = waterType, notes = notes
            ))
            _isAdding.value = false
            _newCoord.value = null
        }
    }

    fun cancelAdd() { _isAdding.value = false; _newCoord.value = null }

    fun selectSpot(spot: FishingSpotEntity) { _selectedSpot.value = spot }

    fun deleteSpot(spot: FishingSpotEntity) {
        viewModelScope.launch { appCtx.spotRepository.delete(spot) }
        _selectedSpot.value = null
    }

    fun copyCoordinates(spot: FishingSpotEntity) {
        val text = "${spot.latitude}, ${spot.longitude}"
        val cm = getApplication<FishBuddyApp>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("coordinates", text))
    }

    fun openInMaps(spot: FishingSpotEntity) {
        // 优先使用高德地图 App，未安装则用网页版
        val amapUri = Uri.parse("androidamap://viewMap?sourceApplication=FishBuddy&lat=${spot.latitude}&lon=${spot.longitude}&dev=0")
        val intent = Intent(Intent.ACTION_VIEW, amapUri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        if (intent.resolveActivity(getApplication<FishBuddyApp>().packageManager) != null) {
            getApplication<FishBuddyApp>().startActivity(intent)
        } else {
            val webUri = Uri.parse("https://uri.amap.com/marker?position=${spot.longitude},${spot.latitude}&name=${Uri.encode(spot.name)}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            getApplication<FishBuddyApp>().startActivity(webIntent)
        }
    }
}
