package com.example.smartsholat.ui.screens.siswa.modul

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartsholat.data.model.DynamicModule
import com.example.smartsholat.ui.screens.guru.modul.ModulAjarViewModel

// ═══════════════════════════════════════════════════
// 1. FUNGSI LOGIKA (STATEFUL)
// ═══════════════════════════════════════════════════

@Composable
fun ModulLainnyaScreen(
    onBackClick: () -> Unit,
    onModulClick: (String) -> Unit
) {
    val viewModel: ModulAjarViewModel = viewModel()
    val modules by viewModel.publishedModules.collectAsState()
    val isLoading by viewModel.isPublishedLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPublishedModules()
    }

    ModulLainnyaContent(
        modules = modules,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onModulClick = onModulClick
    )
}

// ═══════════════════════════════════════════════════
// 2. FUNGSI TAMPILAN (STATELESS)
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulLainnyaContent(
    modules: List<DynamicModule>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onModulClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modul Lainnya", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF388E3C))
                    }
                }
                modules.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada modul belajar\nterbaru",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 16.dp, bottom = 32.dp
                        )
                    ) {
                        items(modules) { modul ->
                            ModulSiswaCard(
                                modul = modul,
                                onClick = { onModulClick(modul.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════
// KOMPONEN: Card Modul untuk Siswa
// ═══════════════════════════════════════════════════

@Composable
fun ModulSiswaCard(
    modul: DynamicModule,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modul.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${modul.category} • ${modul.stepCount} langkah",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mulai",
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = androidx.compose.ui.tooling.preview.Devices.PIXEL_4)
@Composable
private fun ModulLainnyaContentPreview() {
    ModulLainnyaContent(
        modules = listOf(
            DynamicModule(id = "1", title = "Tata Cara Wudhu", category = "Bersuci", stepCount = 6),
            DynamicModule(id = "2", title = "Doa Sehari-hari", category = "Doa", stepCount = 10),
            DynamicModule(id = "3", title = "Adab Berpakaian", category = "Akhlak", stepCount = 4)
        ),
        isLoading = false,
        onBackClick = {},
        onModulClick = {}
    )
}
