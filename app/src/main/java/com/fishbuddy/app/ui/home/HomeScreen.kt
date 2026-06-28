package com.fishbuddy.app.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fishbuddy.app.ui.theme.AppBlue
import java.io.File

@Composable
fun HomeScreen() {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    var photoFile by remember { mutableStateOf<File?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    // --- Camera launcher ---
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            val data = photoFile!!.readBytes()
            vm.onImageCaptured(data)
        }
        showCamera = false
    }

    // --- Gallery picker launcher ---
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val data = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (data != null) {
                vm.onImageCaptured(data)
            }
        }
        vm.dismissCameraOptions()
    }

    // Handle camera permission + launch
    LaunchedEffect(showCamera) {
        if (showCamera) {
            val file = File(context.cacheDir, "fishbuddy_photo.jpg").also { it.parentFile?.mkdirs() }
            photoFile = file
            photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraLauncher.launch(photoUri!!)
        }
    }

    // === SOURCE PICKER DIALOG ===
    if (state.showCameraOptions) {
        AlertDialog(
            onDismissRequest = { vm.dismissCameraOptions() },
            title = { Text("选择图片来源", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = {
                            vm.dismissCameraOptions()
                            showCamera = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("拍照", fontSize = 16.sp)
                    }
                    TextButton(
                        onClick = {
                            vm.dismissCameraOptions()
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Photo, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("从相册选择", fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { vm.dismissCameraOptions() }) {
                    Text("取消")
                }
            }
        )
    }

    // === RESULT SCREEN ===
    if (state.analysisResult != null) {
        AnalysisResultScreen(
            species = state.resultSpecies,
            recommendations = state.resultRecommendations,
            waterType = state.resultWaterType,
            conditions = state.analysisResult!!.waterConditions ?: "",
            locationName = state.analysisResult!!.locationName,
            onDone = { vm.reset() },
            onReanalyze = { vm.reset() }
        )
        return
    }

    // === LOADING OVERLAY ===
    if (state.isAnalyzing) {
        Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.95f)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.WaterDrop, null, tint = AppBlue, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(color = AppBlue)
                Spacer(Modifier.height(12.dp))
                Text(state.loadingMessage, fontWeight = FontWeight.Bold)
                Text("这可能需要几秒钟", color = Color.Gray, fontSize = 12.sp)
            }
        }
        return
    }

    // === HOME SCREEN ===
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        Surface(shape = CircleShape, color = AppBlue.copy(alpha = 0.06f), modifier = Modifier.size(100.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.WaterDrop, null, tint = AppBlue, modifier = Modifier.size(48.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("FishBuddy", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text("拍照识鱼，一钓一个准", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))

        Button(
            onClick = { vm.showCamera() },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
            modifier = Modifier.size(200.dp).shadow(24.dp, CircleShape, spotColor = AppBlue.copy(alpha = 0.35f), ambientColor = AppBlue.copy(alpha = 0.35f))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(40.dp))
                Text("分析鱼情", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("拍摄或选择水域照片", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        Text("📍 自动获取位置信息 · 智能分析鱼情", color = Color.LightGray, fontSize = 11.sp)
        Spacer(Modifier.height(20.dp))
    }
}
