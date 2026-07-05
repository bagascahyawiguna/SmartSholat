package com.example.smartsholat.ui.screens.siswa.home.belajargerakan

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailGerakanStepperContent(
    namaSholat: String,
    onBack: () -> Unit
) {
    // Ambil list gerakan lengkap berdasarkan sholat yang dipilih
    val listGerakan = remember { DataBelajar.getDaftarGerakan(namaSholat) }
    var currentStep by remember { mutableIntStateOf(0) }
    val currentGerakan = listGerakan[currentStep]

    val scrollState = rememberScrollState()

    // ── STATE TTS ──
    val context = LocalContext.current
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale("id", "ID")
                isTtsReady = true
            }
        }
        tts.value = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Reset scroll ke atas setiap kali langkah berubah dan hentikan TTS
    LaunchedEffect(currentStep) {
        tts.value?.stop()
        isSpeaking = false
        scrollState.animateScrollTo(0, animationSpec = tween(durationMillis = 300))
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5), // Set background langsung di Scaffold
        topBar = {
            TopAppBar(
                title = {
                    Text("Belajar Sholat $namaSholat", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF388E3C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },

        bottomBar = {
            // Surface dengan navigationBarsPadding agar tidak tertutup tombol navigasi HP
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = Color.White,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isFirstStep = currentStep == 0

                    OutlinedButton(
                        onClick = { if (!isFirstStep) currentStep-- },
                        modifier = Modifier.weight(1f).height(48.dp),
                        enabled = !isFirstStep,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isFirstStep) Color.LightGray else Color(0xFF388E3C)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF388E3C),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Text("Sebelumnya", fontWeight = FontWeight.Bold)
                    }

                    // Tombol Text-to-Speech
                    IconButton(
                        onClick = {
                            if (isTtsReady) {
                                val engine = tts.value
                                if (engine != null) {
                                    if (engine.isSpeaking) {
                                        engine.stop()
                                        isSpeaking = false
                                    } else {
                                        val teksLatin = currentGerakan.bacaanLatin
                                        val teksArti = currentGerakan.arti
                                        val teksBacaan = "$teksLatin. Artinya: $teksArti"
                                        engine.speak(teksBacaan, TextToSpeech.QUEUE_FLUSH, null, "tts_bacaan")
                                        isSpeaking = true
                                        engine.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                                            override fun onStart(utteranceId: String?) {}
                                            override fun onDone(utteranceId: String?) { isSpeaking = false }
                                            @Deprecated("Deprecated in Java")
                                            override fun onError(utteranceId: String?) { isSpeaking = false }
                                        })
                                    }
                                }
                            }
                        },
                        enabled = isTtsReady,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (isSpeaking) Color(0xFF2E7D32) else Color(0xFF388E3C),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = "Dengarkan Bacaan Latin dan Arti",
                            tint = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            if (currentStep < listGerakan.size - 1) currentStep++ else onBack()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentStep == listGerakan.size - 1) Color(0xFF2E7D32) else Color(0xFF388E3C)
                        )
                    ) {
                        Text(if (currentStep == listGerakan.size - 1) "Selesai" else "Lanjut", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // PERBAIKAN: gunakan padding penuh dari Scaffold (atas & bawah)
                // agar konten tidak tertutup TopBar maupun bottomBar
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / listGerakan.size },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE8F5E9)
            )

            Text(
                text = "Langkah ${currentStep + 1} dari ${listGerakan.size}",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge Rakaat
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "Rakaat ${currentGerakan.rakaat}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentGerakan.namaGerakan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Ilustrasi
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                    AnimatedContent(
                        targetState = currentGerakan.imageRes,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                        },
                        label = "GambarGerakan"
                    ) { imageRes ->
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = "Ilustrasi ${currentGerakan.namaGerakan}",
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = currentGerakan.bacaanArab,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentGerakan.bacaanLatin,
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF388E3C),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${currentGerakan.arti}\"",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    // Spacer tambahan agar konten terbawah tidak mepet saat di-scroll
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(
    name = "Detail Gerakan - Step 1 (Light)",
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_6
)
@Composable
private fun PreviewDetailGerakanStep1() {
    MaterialTheme {
        DetailGerakanStepperContent(
            namaSholat = "Subuh",
            onBack = {}
        )
    }
}

@Preview(
    name = "Detail Gerakan - Step 1 (Dark)",
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_6,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewDetailGerakanStep1Dark() {
    MaterialTheme {
        DetailGerakanStepperContent(
            namaSholat = "Subuh",
            onBack = {}
        )
    }
}

@Preview(
    name = "Detail Gerakan - Small Screen",
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_4A
)
@Composable
private fun PreviewDetailGerakanSmallScreen() {
    MaterialTheme {
        DetailGerakanStepperContent(
            namaSholat = "Dzuhur",
            onBack = {}
        )
    }
}

@Preview(
    name = "Detail Gerakan - Tablet",
    showBackground = true,
    showSystemUi = true,
    device = Devices.TABLET
)
@Composable
private fun PreviewDetailGerakanTablet() {
    MaterialTheme {
        DetailGerakanStepperContent(
            namaSholat = "Ashar",
            onBack = {}
        )
    }
}