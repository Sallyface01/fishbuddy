package com.fishbuddy.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fishbuddy.app.R
import com.fishbuddy.app.data.model.AnalysisRecordEntity
import com.fishbuddy.app.FishBuddyApp
import androidx.compose.ui.platform.LocalContext

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as FishBuddyApp
    val records = remember { mutableStateListOf<AnalysisRecordEntity>() }

    LaunchedEffect(Unit) {
        app.analysisRepository.getAll().collect { records.clear(); records.addAll(it) }
    }

    if (records.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Delete, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Text(stringResource(R.string.history_no_records), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.history_no_records_hint), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
            }
        }
    } else {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.history_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            records.forEach { r ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(r.waterBodyType ?: "?", style = MaterialTheme.typography.titleSmall)
                        Text(r.locationName ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
