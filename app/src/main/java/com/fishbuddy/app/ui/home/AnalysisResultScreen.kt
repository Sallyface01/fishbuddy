package com.fishbuddy.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fishbuddy.app.domain.model.FishSpecies
import com.fishbuddy.app.domain.model.FishingRecommendation
import com.fishbuddy.app.domain.model.WaterBodyType
import com.fishbuddy.app.ui.theme.AppBlue

@Composable
fun AnalysisResultScreen(
    species: List<FishSpecies>,
    recommendations: List<FishingRecommendation>,
    waterType: WaterBodyType,
    conditions: String,
    locationName: String?,
    onDone: () -> Unit,
    onReanalyze: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Water overview
        item {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Water, null, tint = AppBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("水域概况", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("水体类型：${waterType.chineseName}", fontSize = 14.sp)
                    Text("水质简评：$conditions", fontSize = 14.sp, color = Color.Gray)
                    if (locationName != null)
                        Text("位置：$locationName", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Species
        if (species.isNotEmpty()) {
            item {
                Text("可能存在的鱼种 (${species.size}种)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            items(species.size) { i ->
                SpeciesCard(species[i])
            }
        }

        // Recommendations
        if (recommendations.isNotEmpty()) {
            item {
                Text("推荐钓法", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            items(recommendations.size) { i ->
                RecommendationCard(recommendations[i])
            }
        }

        // Buttons
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onDone, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue)) {
                    Text("完成")
                }
                OutlinedButton(onClick = onReanalyze, modifier = Modifier.weight(1f)) {
                    Text("重新分析")
                }
            }
        }
    }
}

@Composable
fun SpeciesCard(s: FishSpecies) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WaterDrop, null, tint = AppBlue)
                Spacer(Modifier.width(8.dp))
                Text(s.commonName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                Text("${(s.confidence * 100).toInt()}%", color = AppBlue, fontSize = 12.sp)
            }
            Text(s.scientificName, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = Color.Gray)
            Text(s.description, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 8.dp)) {
                s.typicalMethods.take(3).forEach { m ->
                    Surface(shape = RoundedCornerShape(6.dp), color = AppBlue.copy(alpha = 0.08f)) {
                        Text(m, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = AppBlue, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(r: FishingRecommendation) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.FitnessCenter, null, tint = AppBlue)
                Spacer(Modifier.width(8.dp))
                Text(r.method, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFF9800).copy(alpha = 0.12f)) {
                    Text(r.difficulty, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color(0xFFFF9800), fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("推荐饵料：${r.bait}", fontSize = 13.sp)
            Text("技巧：${r.technique}", fontSize = 13.sp, color = Color.DarkGray)
            Text("季节：${r.bestSeason} · 时段：${r.bestTimeOfDay}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
