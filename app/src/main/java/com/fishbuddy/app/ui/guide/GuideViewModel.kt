package com.fishbuddy.app.ui.guide

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fishbuddy.app.FishBuddyApp
import com.fishbuddy.app.service.SpeciesDetailJSON
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GuideViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as FishBuddyApp).fishDatabase

    private val _state = MutableStateFlow(GuideState())
    val state: StateFlow<GuideState> = _state

    init { loadSpecies() }

    fun loadSpecies() {
        _state.value = _state.value.copy(
            allSpecies = db.allSpeciesDetails,
            filteredSpecies = db.allSpeciesDetails
        )
    }

    fun search(query: String) {
        _state.value = _state.value.copy(searchText = query)
        applyFilters()
    }

    fun toggleWaterType(type: String) {
        val current = _state.value.selectedWaterType
        _state.value = _state.value.copy(
            selectedWaterType = if (current == type) null else type
        )
        applyFilters()
    }

    fun toggleRegion(region: String) {
        val current = _state.value.selectedRegion
        _state.value = _state.value.copy(
            selectedRegion = if (current == region) null else region
        )
        applyFilters()
    }

    fun clearFilters() {
        _state.value = GuideState(allSpecies = _state.value.allSpecies, filteredSpecies = _state.value.allSpecies)
    }

    private fun applyFilters() {
        val s = _state.value
        var result = s.allSpecies

        s.selectedWaterType?.let { wt ->
            result = result.filter { db.speciesExistsInWaterType(it.commonName, wt) }
        }
        s.selectedRegion?.let { r ->
            result = result.filter { db.speciesExistsInRegion(it.commonName, r) }
        }
        val q = s.searchText.trim()
        if (q.isNotEmpty()) {
            result = result.filter {
                it.commonName.contains(q, true) ||
                it.scientificName.contains(q, true) ||
                it.englishName.contains(q, true)
            }
        }
        _state.value = s.copy(filteredSpecies = result)
    }
}

data class GuideState(
    val allSpecies: List<SpeciesDetailJSON> = emptyList(),
    val filteredSpecies: List<SpeciesDetailJSON> = emptyList(),
    val searchText: String = "",
    val selectedWaterType: String? = null,
    val selectedRegion: String? = null
) {
    val waterTypes = listOf("河流", "湖泊", "池塘", "水库", "溪流")
    val regions get() = listOf("华东", "华南", "华中", "华北", "西南", "东北", "西北")
    val hasFilters get() = selectedWaterType != null || selectedRegion != null || searchText.isNotEmpty()
}
