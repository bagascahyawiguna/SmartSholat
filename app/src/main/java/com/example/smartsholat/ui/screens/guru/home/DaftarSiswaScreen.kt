package com.example.smartsholat.ui.screens.guru.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsholat.ui.screens.guru.DataSiswa
import com.example.smartsholat.ui.screens.guru.GuruViewModel

// --- 1. FUNGSI LOGIKA (STATEFUL) ---
@Composable
fun DaftarSiswaScreen(
    namaKelas: String?,
    viewModel: GuruViewModel,
    onBackClick: () -> Unit,
    onSiswaClick: (String) -> Unit
) {
    LaunchedEffect(namaKelas) {
        if (namaKelas != null) {
            viewModel.fetchSiswaByKelas(namaKelas)
        }
    }

    // Panggil fungsi Stateless dengan data dari ViewModel
    DaftarSiswaContent(
        namaKelas = namaKelas,
        isSiswaLoading = viewModel.isSiswaLoading,
        daftarSiswa = viewModel.daftarSiswa,
        onBackClick = onBackClick,
        onSiswaClick = onSiswaClick
    )
}

// --- 2. FUNGSI TAMPILAN (STATELESS - UI ONLY) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaftarSiswaContent(
    namaKelas: String?,
    isSiswaLoading: Boolean,
    daftarSiswa: List<DataSiswa>,
    onBackClick: () -> Unit,
    onSiswaClick: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daftar Siswa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Kelas ${namaKelas ?: ""}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            if (isSiswaLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF388E3C))
                    }
                }
            } else if (daftarSiswa.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada data siswa di kelas ini.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 32.dp
                    )
                ) {
                    // Urutkan nama siswa A-Z (case-insensitive)
                    val siswaTerurut = daftarSiswa.sortedBy { it.nama.uppercase() }
                    items(siswaTerurut) { siswa ->
                        SiswaCard(
                            siswa = siswa,
                            onClick = { onSiswaClick(siswa.id) }
                        )
                    }
                }
            }
        }
    }
}

// --- KOMPONEN KECIL ---
@Composable
fun SiswaCard(siswa: DataSiswa, onClick: () -> Unit) {
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
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = siswa.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "NIS: ${siswa.nis}", fontSize = 13.sp, color = Color.Gray)
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Riwayat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 3. FUNGSI PREVIEW (AMAN & HD) ---
@Preview(showBackground = true, device = Devices.PIXEL_4, showSystemUi = true)
@Composable
fun DaftarSiswaContentPreview() {
    val dummySiswa = listOf(
        DataSiswa(id = "1", nama = "Bagas", nis = "2023001", kelas = "Kelas A (Tunawicara)"),
        DataSiswa(id = "2", nama = "Ahmad Maulana", nis = "2023002", kelas = "Kelas A (Tunawicara)"),
        DataSiswa(id = "3", nama = "Siti Aisyah", nis = "2023003", kelas = "Kelas A (Tunawicara)")
    )

    Surface {
        DaftarSiswaContent(
            namaKelas = "Kelas A (Tunawicara)",
            isSiswaLoading = false,
            daftarSiswa = dummySiswa,
            onBackClick = {},
            onSiswaClick = {}
        )
    }
}