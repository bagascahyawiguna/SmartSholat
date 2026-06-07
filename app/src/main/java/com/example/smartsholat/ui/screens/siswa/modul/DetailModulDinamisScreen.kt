package com.example.smartsholat.ui.screens.siswa.modul

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartsholat.ui.screens.guru.modul.ModulAjarViewModel

// ═══════════════════════════════════════════════════
// 1. FUNGSI LOGIKA (STATEFUL)
// ═══════════════════════════════════════════════════

@Composable
fun DetailModulDinamisScreen(
    moduleId: String,
    onBackClick: () -> Unit
) {
    val viewModel: ModulAjarViewModel = viewModel()
    val module by viewModel.currentModule.collectAsState()
    val steps by viewModel.currentSteps.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val isLoading by viewModel.isDetailLoading.collectAsState()

    LaunchedEffect(moduleId) {
        viewModel.loadModuleDetail(moduleId)
    }

    DetailModulDinamisContent(
        title = module?.title ?: "Memuat...",
        steps = steps,
        currentStepIndex = currentStepIndex,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onNextStep = { viewModel.nextStep() },
        onPreviousStep = { viewModel.previousStep() },
        onFinish = onBackClick
    )
}

// ═══════════════════════════════════════════════════
// 2. FUNGSI TAMPILAN (STATELESS)
// Mengikuti pola DetailGerakanStepperContent
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailModulDinamisContent(
    title: String,
    steps: List<com.example.smartsholat.data.model.ModuleStep>,
    currentStepIndex: Int,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onFinish: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Reset scroll ke atas setiap kali langkah berubah
    LaunchedEffect(currentStepIndex) {
        scrollState.animateScrollTo(0, animationSpec = tween(durationMillis = 300))
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            if (!isLoading && steps.isNotEmpty()) {
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val isFirstStep = currentStepIndex == 0
                        val isLastStep = currentStepIndex == steps.size - 1

                        OutlinedButton(
                            onClick = onPreviousStep,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = !isFirstStep,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isFirstStep) Color.LightGray else Color(0xFF388E3C)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF388E3C),
                                disabledContentColor = Color.Gray
                            )
                        ) {
                            Text("Sebelumnya", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { if (isLastStep) onFinish() else onNextStep() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLastStep) Color(0xFF2E7D32) else Color(0xFF388E3C)
                            )
                        ) {
                            Text(
                                if (isLastStep) "Selesai" else "Selanjutnya",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF388E3C))
            }
        } else if (steps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Modul ini belum memiliki langkah.", color = Color.Gray)
            }
        } else {
            val currentStep = steps[currentStepIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentStepIndex + 1).toFloat() / steps.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE8F5E9)
                )

                Text(
                    text = "Langkah ${currentStepIndex + 1} dari ${steps.size}",
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF388E3C)
                )

                // Card konten langkah
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
                        // Judul Langkah
                        Text(
                            text = currentStep.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Deskripsi
                        Text(
                            text = currentStep.description,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF424242),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bacaan Arab (jika tidak kosong)
                        if (currentStep.arabicText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = currentStep.arabicText,
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 40.sp
                                )
                            }
                        }

                        // Bacaan Latin (jika tidak kosong)
                        if (currentStep.latinText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = currentStep.latinText,
                                fontSize = 15.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF388E3C),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Arti / Terjemahan (jika tidak kosong)
                        if (currentStep.translationText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"${currentStep.translationText}\"",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Spacer tambahan agar konten terbawah tidak mepet saat di-scroll
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = androidx.compose.ui.tooling.preview.Devices.PIXEL_4)
@Composable
private fun DetailModulDinamisContentPreview() {
    DetailModulDinamisContent(
        title = "Tata Cara Wudhu",
        steps = listOf(
            com.example.smartsholat.data.model.ModuleStep(
                id = "1", title = "Membasuh Kedua Tangan",
                description = "Basuh kedua tangan hingga pergelangan sebanyak tiga kali.",
                arabicText = "بِسْمِ اللَّهِ",
                latinText = "Bismillah",
                translationText = "Dengan menyebut nama Allah"
            ),
            com.example.smartsholat.data.model.ModuleStep(
                id = "2", title = "Berkumur-kumur",
                description = "Masukkan air ke dalam mulut lalu kumur-kumur sebanyak tiga kali.",
                arabicText = "", latinText = "", translationText = ""
            )
        ),
        currentStepIndex = 0,
        isLoading = false,
        onBackClick = {},
        onNextStep = {},
        onPreviousStep = {},
        onFinish = {}
    )
}
