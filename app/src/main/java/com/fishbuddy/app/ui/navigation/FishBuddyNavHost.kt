package com.fishbuddy.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.fishbuddy.app.R
import com.fishbuddy.app.ui.home.HomeScreen
import com.fishbuddy.app.ui.spots.SpotsScreen
import com.fishbuddy.app.ui.guide.GuideScreen
import com.fishbuddy.app.ui.history.HistoryScreen
import com.fishbuddy.app.ui.settings.SettingsScreen

enum class Tab(val labelRes: Int, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    ANALYZE(R.string.tab_analyze, Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
    SPOTS(R.string.tab_spots, Icons.Filled.Place, Icons.Outlined.Place),
    GUIDE(R.string.tab_guide, Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    HISTORY(R.string.tab_history, Icons.Filled.History, Icons.Outlined.History),
    SETTINGS(R.string.tab_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishBuddyNavHost() {
    var selectedTab by remember { mutableStateOf(Tab.ANALYZE) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = stringResource(tab.labelRes)
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                Tab.ANALYZE -> HomeScreen()
                Tab.SPOTS -> SpotsScreen()
                Tab.GUIDE -> GuideScreen()
                Tab.HISTORY -> HistoryScreen()
                Tab.SETTINGS -> SettingsScreen()
            }
        }
    }
}
