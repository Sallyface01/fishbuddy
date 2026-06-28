package com.fishbuddy.app.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fishbuddy.app.service.SpeciesDetailJSON
import com.fishbuddy.app.ui.theme.AppBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen() {
    val vm: GuideViewModel = viewModel()
    val state by vm.state.collectAsState()
    var selectedSpecies by remember { mutableStateOf<SpeciesDetailJSON?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // Search bar
        OutlinedTextField(
            value = state.searchText,
            onValueChange = { vm.search(it) },
            placeholder = { Text("搜索鱼种名称") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = { if (state.searchText.isNotEmpty()) IconButton({ vm.search("") }) { Icon(Icons.Filled.Close, null) } },
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        // Water type chips
        ScrollableTabRow(selectedTabIndex = state.waterTypes.indexOf(state.selectedWaterType).coerceAtLeast(0),
            modifier = Modifier.padding(horizontal = 12.dp), edgePadding = 0.dp) {
            state.waterTypes.forEach { wt ->
                Tab(
                    selected = state.selectedWaterType == wt,
                    onClick = { vm.toggleWaterType(wt) },
                    text = { Text(wt, fontSize = 13.sp) }
                )
            }
        }

        // Region + clear + count
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.Map, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(state.selectedRegion ?: "全部区域", fontSize = 13.sp)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    state.regions.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = { vm.toggleRegion(r); expanded = false },
                            leadingIcon = if (state.selectedRegion == r) {{ Icon(Icons.Filled.Check, null) }} else null
                        )
                    }
                }
            }
            if (state.hasFilters) {
                TextButton(onClick = { vm.clearFilters() }) { Text("清除筛选", color = Color.Red, fontSize = 13.sp) }
            }
            Spacer(Modifier.weight(1f))
            Text("${state.filteredSpecies.size} 种", color = Color.Gray, fontSize = 13.sp)
        }

        // Grid
        if (state.filteredSpecies.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.SearchOff, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Text("没有找到匹配的鱼种", color = Color.Gray)
                    Text("尝试调整筛选条件或搜索词", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Adaptive(160.dp), modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.filteredSpecies) { species ->
                    Card(onClick = { selectedSpecies = species },
                        shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.WaterDrop, null, tint = AppBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(species.commonName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text(species.scientificName, fontStyle = FontStyle.Italic, fontSize = 10.sp, color = Color.Gray)
                            Text(species.description, fontSize = 11.sp, color = Color.DarkGray, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
                            Row(Modifier.padding(top = 6.dp)) {
                                species.typicalMethods.take(2).forEach { m ->
                                    Surface(shape = RoundedCornerShape(4.dp), color = AppBlue.copy(alpha = 0.08f)) {
                                        Text(m, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = AppBlue, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Species detail sheet
    if (selectedSpecies != null) {
        SpeciesDetailSheet(selectedSpecies!!) { selectedSpecies = null }
    }
}
