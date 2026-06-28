package com.fishbuddy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fishbuddy.app.ui.navigation.FishBuddyNavHost
import com.fishbuddy.app.ui.theme.FishBuddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FishBuddyTheme {
                FishBuddyNavHost()
            }
        }
    }
}
