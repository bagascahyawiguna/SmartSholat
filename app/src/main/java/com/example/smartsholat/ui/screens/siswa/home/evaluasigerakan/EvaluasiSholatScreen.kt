package com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartsholat.ui.screens.siswa.home.belajargerakan.PilihSholatListContent
import com.example.smartsholat.utils.SessionManager
import kotlinx.coroutines.delay

// Data class diletakkan di sini
data class HasilEvaluasiData(
    val namaSholat: String,
    val durasi: Long,
    val totalGagal: Int,
    val gerakanGagal: String
)

// Representasi state layar untuk AnimatedContent
private enum class EvaluasiScreenState { PILIH, KAMERA, HASIL }

@Composable
fun EvaluasiSholatScreen(onBackClick: () -> Unit) {
    val context        = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var sholatTerpilih      by remember { mutableStateOf<String?>(null) }
    var resultData          by remember { mutableStateOf<HasilEvaluasiData?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    val currentResult = resultData

    BackHandler(enabled = true) {
        if (currentResult != null) {
            resultData     = null
            sholatTerpilih = null
        } else if (sholatTerpilih != null) {
            sholatTerpilih = null
        } else {
            onBackClick()
        }
    }

    // Tentukan state layar aktif
    val screenState = when {
        currentResult != null -> EvaluasiScreenState.HASIL
        sholatTerpilih != null -> EvaluasiScreenState.KAMERA
        else -> EvaluasiScreenState.PILIH
    }

    AnimatedContent(
        targetState = screenState,
        transitionSpec = {
            when {
                // Pilih → Kamera atau Pilih → Hasil: slide masuk dari kanan
                initialState == EvaluasiScreenState.PILIH -> {
                    (slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)))
                }
                // Kamera/Hasil → Pilih: slide masuk dari kiri
                targetState == EvaluasiScreenState.PILIH -> {
                    (slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)))
                }
                // Kamera → Hasil: fade saja (transisi ringan setelah kamera)
                else -> {
                    (fadeIn(tween(250))).togetherWith(fadeOut(tween(250)))
                }
            }
        },
        label = "EvaluasiNavigation"
    ) { state ->
        when (state) {
            EvaluasiScreenState.HASIL -> {
                val result = currentResult ?: return@AnimatedContent
                ResultEvaluasiScreen(
                    namaSholat   = result.namaSholat,
                    durasiMillis = result.durasi,
                    totalGagal   = result.totalGagal,
                    gerakanGagal = result.gerakanGagal,
                    onFinish     = {
                        resultData     = null
                        sholatTerpilih = null
                    }
                )
            }
            EvaluasiScreenState.KAMERA -> {
                val namaSholat = sholatTerpilih ?: return@AnimatedContent
                if (hasCameraPermission) {
                    CameraEvaluasiContent(
                        namaSholat        = namaSholat,
                        sessionManager    = sessionManager,
                        onBack            = { sholatTerpilih = null },
                        onEvaluasiSelesai = { nama, durasi, totalGagal, teksGagal ->
                            resultData = HasilEvaluasiData(nama, durasi, totalGagal, teksGagal)
                        }
                    )
                } else {
                    // Izin kamera ditolak — tampilkan pesan informatif lalu kembali
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Izin kamera diperlukan untuk fitur ini.\nSilakan aktifkan di Pengaturan aplikasi.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                    LaunchedEffect(Unit) {
                        delay(2000)
                        onBackClick()
                    }
                }
            }
            EvaluasiScreenState.PILIH -> {
                PilihSholatListContent(
                    onSholatSelect = { sholatTerpilih = it },
                    onBack         = onBackClick
                )
            }
        }
    }
}