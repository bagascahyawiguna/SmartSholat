package com.example.smartsholat.ui.screens.siswa.home.belajargerakan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*

@Composable
fun BelajarSholatScreen(onBackClick: () -> Unit) {
    var sholatTerpilih by remember { mutableStateOf<String?>(null) }

    // HANDLE BACK BUTTON SMARTPHONE
    BackHandler(enabled = true) {
        if (sholatTerpilih != null) {
            sholatTerpilih = null
        } else {
            onBackClick()
        }
    }

    AnimatedContent(
        targetState = sholatTerpilih,
        transitionSpec = {
            if (initialState == null && targetState != null) {
                // Masuk ke detail: slide in dari kanan
                (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut(tween(300)))
            } else {
                // Kembali ke list: slide in dari kiri
                (slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(tween(300)))
            }
        },
        label = "BelajarNavigation"
    ) { sholat ->
        if (sholat == null) {
            PilihSholatListContent(
                onSholatSelect = { sholatTerpilih = it },
                onBack = onBackClick
            )
        } else {
            DetailGerakanStepperContent(
                namaSholat = sholat,
                onBack = { sholatTerpilih = null }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = androidx.compose.ui.tooling.preview.Devices.PIXEL_4)
@Composable
private fun BelajarSholatScreenPreview() {
    BelajarSholatScreen(onBackClick = {})
}
