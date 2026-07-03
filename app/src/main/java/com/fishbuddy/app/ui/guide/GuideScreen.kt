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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fishbuddy.app.service.SpeciesDetailJSON
import com.fishbuddy.app.ui.theme.AppBlue

/**
 * 获取鱼种图片的数据源，优先级：
 * 1. JSON 中显式指定的 imageUrl
 * 2. 本地 assets/images/ 中打包的图片（ByteArray）
 * 3. Wikimedia Commons 远程 URL
 */
private fun speciesImageModel(species: SpeciesDetailJSON, context: android.content.Context): Any {
    if (species.imageUrl != null) return species.imageUrl
    val cleanName = species.scientificName.split(" / ").first().replace(" ", "_")
    val assetPath = "images/${cleanName}.jpg"
    return try {
        context.assets.open(assetPath).use { it.readBytes() }
    } catch (_: java.io.IOException) {
        "https://commons.wikimedia.org/wiki/Special:FilePath/${cleanName}?width=400"
    }
}

/** 用鱼种名称生成一个稳定的颜色，用于兜底头像 */
private fun fallbackColor(name: String): Color {
    val hue = name.hashCode().and(0x7FFFFFFF) % 360
    return Color.hsl(hue.toFloat(), 0.45f, 0.72f)
}

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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp), modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredSpecies) { species ->
                    GuideCard(species = species, onClick = { selectedSpecies = species })
                }
            }
        }
    }

    // Species detail sheet
    if (selectedSpecies != null) {
        SpeciesDetailSheet(selectedSpecies!!) { selectedSpecies = null }
    }
}

@Composable
private fun GuideCard(species: SpeciesDetailJSON, onClick: () -> Unit) {
    val context = LocalContext.current
    val imageModel = remember(species) { speciesImageModel(species, context) }
    val cardColor = fallbackColor(species.commonName)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- Image with styled fallback ---
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(cardColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Styled avatar: first Chinese character in colored circle
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = cardColor.copy(alpha = 0.18f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = species.commonName.first().toString(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardColor
                            )
                        }
                    }
                    // English name below
                    Text(
                        text = species.englishName,
                        fontSize = 9.sp,
                        color = cardColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // Image overlay — covers fallback when loaded
                AsyncImage(
                    model = ImageRequest.Builder(context).data(imageModel).crossfade(true).build(),
                    contentDescription = species.commonName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // --- Info ---
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
