package com.fishbuddy.app.ui.guide

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fishbuddy.app.FishBuddyApp
import com.fishbuddy.app.data.model.CatchLogEntity
import com.fishbuddy.app.service.SpeciesDetailJSON
import com.fishbuddy.app.ui.theme.AppBlue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SpeciesDetailSheet(species: SpeciesDetailJSON, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FishBuddyApp
    val catchLogs = remember { mutableStateListOf<CatchLogEntity>() }
    var showAddCatch by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load catch logs
    LaunchedEffect(species.commonName) {
        app.catchLogRepository.getForSpecies(species.commonName).collectLatest {
            catchLogs.clear(); catchLogs.addAll(it)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // Top bar
        Surface(shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(species.commonName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, "关闭") }
            }
        }

        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.WaterDrop, null, tint = AppBlue, modifier = Modifier.size(64.dp))
                    Text(species.commonName, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(species.englishName, color = Color.Gray)
                    Text(species.scientificName, fontStyle = FontStyle.Italic, fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Description
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Label("简介")
                        Text(species.description, fontSize = 14.sp)
                    }
                }
            }

            // Methods
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Label("推荐钓法")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            species.typicalMethods.forEach { m ->
                                Surface(shape = RoundedCornerShape(6.dp), color = AppBlue.copy(alpha = 0.08f)) {
                                    Text(m, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = AppBlue, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Bait
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Label("推荐饵料")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            species.bestBait.forEach { b ->
                                Text(b, Modifier.padding(horizontal = 8.dp, vertical = 4.dp).background(Color(0xFFFF9800).copy(alpha = 0.08f), RoundedCornerShape(4.dp)),
                                    color = Color(0xFFE65100), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Season info
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row { Text("最佳季节：", color = Color.Gray); Text(species.bestSeason) }
                        Spacer(Modifier.height(4.dp))
                        Row { Text("最佳时段：", color = Color.Gray); Text(species.bestTime) }
                        Spacer(Modifier.height(4.dp))
                        Row { Text("常见体型：", color = Color.Gray); Text(species.avgSize) }
                    }
                }
            }

            // Catch log
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Label("我的捕获记录")
                            Spacer(Modifier.weight(1f))
                            Text("${catchLogs.size} 次", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { showAddCatch = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppBlue.copy(alpha = 0.08f), contentColor = AppBlue),
                            modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("记录捕获")
                        }
                        catchLogs.forEach { log ->
                            Divider(Modifier.padding(vertical = 8.dp))
                            Row {
                                Column(Modifier.weight(1f)) {
                                    Text(log.notes.ifEmpty { "(无文字记录)" }, fontSize = 13.sp)
                                    Text(log.locationName ?: "", fontSize = 11.sp, color = Color.Gray)
                                    Text(java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.CHINA).format(log.date), fontSize = 10.sp, color = Color.Gray)
                                }
                                IconButton(onClick = {
                                    scope.launch { app.catchLogRepository.delete(log) }
                                }) {
                                    Icon(Icons.Filled.Delete, "删除", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add catch dialog
    if (showAddCatch) {
        var notes by remember { mutableStateOf("") }
        var locName by remember { mutableStateOf("") }
        var photoData by remember { mutableStateOf<ByteArray?>(null) }

        AlertDialog(
            onDismissRequest = { showAddCatch = false },
            title = { Text("记录捕获 · ${species.commonName}") },
            text = {
                Column {
                    OutlinedTextField(notes, { notes = it }, label = { Text("心得记录") }, maxLines = 3)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(locName, { locName = it }, label = { Text("位置（选填）") })
                }
            },
            confirmButton = {
                TextButton({
                    scope.launch {
                        app.catchLogRepository.insert(CatchLogEntity(
                            speciesName = species.commonName,
                            photoData = photoData,
                            notes = notes,
                            locationName = locName.ifEmpty { null }
                        ))
                    }
                    showAddCatch = false
                }) { Text("保存") }
            },
            dismissButton = { TextButton({ showAddCatch = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(text, fontWeight = FontWeight.Bold, color = AppBlue, fontSize = 15.sp)
}
