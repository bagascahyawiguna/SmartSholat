package com.example.smartsholat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Skema warna tetap — tidak terpengaruh dark mode sistem maupun dynamic color (Material You)
private val AppColorScheme = lightColorScheme(
    primary          = Color(0xFF388E3C),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary        = Color(0xFF4CAF50),
    onSecondary      = Color.White,
    background       = Color(0xFFF5F5F5),
    onBackground     = Color(0xFF1C1B1F),
    surface          = Color.White,
    onSurface        = Color(0xFF1C1B1F),
    surfaceVariant   = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF49454F),
    error            = Color(0xFFB00020),
    onError          = Color.White,
    outline          = Color(0xFF79747E)
)

@Composable
fun SmartSholatTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography  = Typography,
        content     = content
    )
}