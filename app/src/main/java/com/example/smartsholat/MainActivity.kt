package com.example.smartsholat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.smartsholat.ui.navigation.AppNavigation
import com.example.smartsholat.ui.theme.SmartSholatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartSholatTheme {
                AppNavigation()
            }
        }
    }
}