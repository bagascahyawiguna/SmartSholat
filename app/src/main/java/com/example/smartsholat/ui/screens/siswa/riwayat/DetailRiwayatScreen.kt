package com.example.smartsholat.ui.screens.siswa.riwayat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import java.util.concurrent.TimeUnit

private val HijauUtama  = Color(0xFF388E3C)
private val HijauGelap  = Color(0xFF2E7D32)
private val HijauMuda   = Color(0xFFE8F5E9)
private val MerahUtama  = Color(0xFFD32F2F)
private val MerahMuda   = Color(0xFFFFEBEE)
private val MerahBorder = Color(0xFFFFCDD2)
private val AbuBg       = Color(0xFFF5F5F5)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRiwayatScreen(
    riwayat: RiwayatItem,
    onBackClick: () -> Unit
) {
    val totalDetik = TimeUnit.MILLISECONDS.toSeconds(riwayat.durasiMillis)
    val menit = totalDetik / 60
    val detik = totalDetik % 60
    val formatDurasi = String.format(Locale.getDefault(), "%02d mnt %02d dtk", menit, detik)

    val adaGagal = riwayat.TotalGerakanGagal > 0

    val displayGerakanGagal = when {
        riwayat.GerakanGagal.contains("\n") -> riwayat.GerakanGagal
        riwayat.GerakanGagal.isNotBlank() ->
            riwayat.GerakanGagal
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapIndexed { i, text -> "${i + 1}. $text" }
                .joinToString("\n")
        else -> ""
    }

    Scaffold(
        topBar = {
            // HEADER TETAP DIPERTAHANKAN
            TopAppBar(
                title = { Text("Detail Evaluasi", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HijauUtama,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AbuBg)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- 1. KARTU INFO UTAMA ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = riwayat.namaSholat,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = HijauGelap,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = if (adaGagal) MerahMuda else HijauMuda,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (adaGagal) "${riwayat.TotalGerakanGagal} Gagal" else "Sempurna",
                                color = if (adaGagal) MerahUtama else HijauGelap,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(Modifier.height(16.dp))

                    InfoRow(icon = Icons.Default.CalendarToday, label = "Waktu Pengerjaan", value = riwayat.tanggal)
                    Spacer(Modifier.height(14.dp))
                    InfoRow(icon = Icons.Default.AccessTime, label = "Durasi Keseluruhan", value = formatDurasi)
                }
            }

            // --- 2. KARTU HASIL EVALUASI ---
            Text(
                text = "Hasil Evaluasi Gerakan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            if (!adaGagal) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = HijauMuda),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(HijauUtama.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HijauUtama, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("Alhamdulillah! Gerakan Sempurna", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = HijauGelap)
                        Spacer(Modifier.height(6.dp))
                        Text("Tidak ada gerakan yang gagal atau terlewat.", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MerahMuda),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(MerahUtama.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MerahUtama, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Terdapat Gerakan Kurang Tepat", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MerahUtama)
                                Text("${riwayat.TotalGerakanGagal} gerakan perlu diperbaiki", fontSize = 12.sp, color = MerahUtama.copy(alpha = 0.8f))
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MerahBorder)
                        Spacer(Modifier.height(14.dp))

                        Text("Gerakan yang perlu diperbaiki:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        Spacer(Modifier.height(10.dp))

                        // Render list rapi dan responsif
                        displayGerakanGagal.lines().forEach { baris ->
                            if (baris.isNotBlank()) {
                                val isHeader = baris.startsWith("Rakaat")
                                Row(
                                    modifier = Modifier.padding(top = if (isHeader) 8.dp else 2.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    if (!isHeader) {
                                        Box(
                                            modifier = Modifier.padding(top = 6.dp, end = 8.dp).size(6.dp).clip(CircleShape).background(MerahUtama.copy(alpha = 0.7f))
                                        )
                                    }
                                    Text(
                                        text = if (isHeader) baris else baris.removePrefix("${baris.substringBefore('.')}. ").let { baris },
                                        fontSize = if (isHeader) 13.sp else 14.sp,
                                        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isHeader) MerahUtama else Color.DarkGray,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(HijauMuda),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = HijauUtama, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = androidx.compose.ui.tooling.preview.Devices.PIXEL_4)
@Composable
private fun DetailRiwayatScreenPreview() {
    DetailRiwayatScreen(
        riwayat = RiwayatItem(
            id = "1",
            namaSholat = "Sholat Dzuhur",
            tanggal = "07 Juni 2026, 12:30",
            durasiMillis = 185000,
            TotalGerakanGagal = 3,
            GerakanGagal = "Rakaat 1\n1. Rukuk - posisi punggung kurang lurus\n2. Sujud - posisi tangan salah\nRakaat 2\n3. I'tidal - tidak tegak sempurna"
        ),
        onBackClick = {}
    )
}