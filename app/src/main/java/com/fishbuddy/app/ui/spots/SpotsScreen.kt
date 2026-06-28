package com.fishbuddy.app.ui.spots

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.fishbuddy.app.data.model.FishingSpotEntity
import com.fishbuddy.app.domain.model.WaterBodyType
import com.fishbuddy.app.ui.theme.AppBlue

@Composable
fun SpotsScreen() {
    val vm: SpotsViewModel = viewModel()
    val spots by vm.spots.collectAsState()
    val isAdding by vm.isAdding.collectAsState()
    val newCoord by vm.newCoord.collectAsState()
    val selectedSpot by vm.selectedSpot.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // MapView lifecycle management (pause / resume / destroy)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDestroy()
        }
    }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 3.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("我的钓点 · ${spots.size}个", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (spots.isEmpty()) {
                        Text("还没有收藏的钓点\n长按地图添加钓点", color = Color.Gray, fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(spots.size) { i ->
                                val s = spots[i]
                                Row(Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Water, null, tint = AppBlue, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(s.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${s.latitude}, ${s.longitude}", color = Color.Gray, fontSize = 11.sp)
                                    }
                                    Text(WaterBodyType.from(s.waterBodyType ?: "").chineseName,
                                        color = AppBlue, fontSize = 11.sp)
                                }
                                if (i < spots.lastIndex) HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->

        // === 高德 MapView ===
        AndroidView(
            factory = { ctx ->
                MapView(ctx).also { mapView = it }.apply {
                    onCreate(Bundle())
                    onResume()
                    map?.apply {
                        uiSettings.isZoomControlsEnabled = false
                        moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.0, 105.0), 4f))
                        setOnMapLongClickListener { latLng -> vm.addSpot(latLng) }
                    }
                }
            },
            update = { mv ->
                mv.map?.let { map ->
                    map.clear()
                    spots.forEach { spot ->
                        map.addMarker(
                            MarkerOptions()
                                .position(LatLng(spot.latitude, spot.longitude))
                                .title(spot.name)
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }

    // Add spot dialog
    if (isAdding && newCoord != null) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(WaterBodyType.LAKE.chineseName) }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { vm.cancelAdd() },
            title = { Text("添加钓点") },
            text = {
                Column {
                    OutlinedTextField(name, { name = it }, label = { Text("钓点名称") })
                    Spacer(Modifier.height(8.dp))
                    Row {
                        WaterBodyType.entries.filter { it != WaterBodyType.UNKNOWN }.forEach { wt ->
                            FilterChip(wt == WaterBodyType.from(type), { type = wt.chineseName },
                                { Text(wt.chineseName) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(notes, { notes = it }, label = { Text("备注（选填）") })
                }
            },
            confirmButton = { TextButton({ vm.saveSpot(name, type, notes) }) { Text("保存") } },
            dismissButton = { TextButton({ vm.cancelAdd() }) { Text("取消") } }
        )
    }

    // Spot detail dialog
    if (selectedSpot != null) {
        val spot = selectedSpot!!
        AlertDialog(
            onDismissRequest = { vm.selectSpot(spot) },
            title = { Text(spot.name) },
            text = {
                Column {
                    Text("位置：${spot.latitude}, ${spot.longitude}", fontSize = 13.sp)
                    Text("水域：${WaterBodyType.from(spot.waterBodyType ?: "").chineseName}", fontSize = 13.sp)
                    spot.notes?.let { Text("备注：$it", fontSize = 13.sp) }
                }
            },
            confirmButton = { TextButton({ vm.copyCoordinates(spot); vm.selectSpot(spot) }) { Text("复制坐标") } },
            dismissButton = {
                Row {
                    TextButton({ vm.openInMaps(spot) }) { Text("导航") }
                    TextButton({ vm.deleteSpot(spot) }) { Text("删除", color = Color.Red) }
                }
            }
        )
    }
}
