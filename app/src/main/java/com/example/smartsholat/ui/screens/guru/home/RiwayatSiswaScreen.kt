package com.example.smartsholat.ui.screens.guru.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsholat.ui.screens.guru.GuruViewModel
import java.util.Locale
import java.util.concurrent.TimeUnit

// ── Warna ─────────────────────────────────────────────────────
private val HijauUtama   = Color(0xFF388E3C)
private val HijauGelap   = Color(0xFF2E7D32)
private val HijauMuda    = Color(0xFFE8F5E9)
private val MerahUtama   = Color(0xFFD32F2F)
private val MerahMuda    = Color(0xFFFFEBEE)
private val AbuBg        = Color(0xFFF5F5F5)

// ─────────────────────────────────────────────────────────────
// 1. STATEFUL — menghubungkan ViewModel dengan UI
// ─────────────────────────────────────────────────────────────
@Composable
fun RiwayatSiswaScreen(
    siswaId: String?,
    viewModel: GuruViewModel,
    onBackClick: () -> Unit,
    onItemClick: (GuruViewModel.DataRiwayat) -> Unit   // ← navigasi ke detail
) {
    LaunchedEffect(siswaId) {
        if (siswaId != null) viewModel.fetchRiwayatSiswa(siswaId)
    }

    RiwayatSiswaContent(
        namaSiswaTerpilih = viewModel.namaSiswaTerpilih,
        isRiwayatLoading  = viewModel.isRiwayatLoading,
        daftarRiwayat     = viewModel.daftarRiwayat,
        onBackClick       = onBackClick,
        onItemClick       = onItemClick
    )
}

// ─────────────────────────────────────────────────────────────
// 2. STATELESS — murni UI
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatSiswaContent(
    namaSiswaTerpilih: String,
    isRiwayatLoading: Boolean,
    daftarRiwayat: List<GuruViewModel.DataRiwayat>,
    onBackClick: () -> Unit,
    onItemClick: (GuruViewModel.DataRiwayat) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Riwayat Evaluasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = namaSiswaTerpilih,
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
                    containerColor = HijauUtama,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AbuBg)
                .padding(paddingValues)
        ) {
            when {
                isRiwayatLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HijauUtama)
                    }
                }

                daftarRiwayat.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = Color.LightGray
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Belum ada riwayat evaluasi",
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                else -> {
                    val totalSempurna = daftarRiwayat.count { it.TotalGerakanGagal == 0 }
                    val totalAdaGagal = daftarRiwayat.size - totalSempurna

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 32.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            StatistikRingkasanCard(
                                total = daftarRiwayat.size,
                                totalSempurna = totalSempurna,
                                totalAdaGagal = totalAdaGagal
                            )
                            Spacer(Modifier.height(4.dp))
                        }

                        item {
                            Text(
                                text = "Daftar Evaluasi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                            )
                        }

                        items(daftarRiwayat) { riwayat ->
                            RiwayatCard(
                                riwayat = riwayat,
                                onClick = { onItemClick(riwayat) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// KARTU STATISTIK RINGKASAN
// ─────────────────────────────────────────────────────────────
@Composable
private fun StatistikRingkasanCard(
    total: Int,
    totalSempurna: Int,
    totalAdaGagal: Int
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatistikItem(angka = total,         label = "Total",   warna = HijauGelap)
            VerticalDivider(Modifier.height(40.dp))
            StatistikItem(angka = totalSempurna, label = "Sempurna", warna = HijauUtama)
            VerticalDivider(Modifier.height(40.dp))
            StatistikItem(angka = totalAdaGagal, label = "Ada Gagal", warna = MerahUtama)
        }
    }
}

@Composable
private fun StatistikItem(angka: Int, label: String, warna: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = angka.toString(),
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = warna
        )
        Text(
            text     = label,
            fontSize = 12.sp,
            color    = Color.Gray
        )
    }
}

// ─────────────────────────────────────────────────────────────
// KARTU RIWAYAT (clickable)
// ─────────────────────────────────────────────────────────────
@Composable
fun RiwayatCard(
    riwayat: GuruViewModel.DataRiwayat,
    onClick: () -> Unit
) {
    val totalDetik   = TimeUnit.MILLISECONDS.toSeconds(riwayat.durasiMillis)
    val menit        = totalDetik / 60
    val detik        = totalDetik % 60
    val formatDurasi = String.format(Locale.getDefault(), "%02d:%02d", menit, detik)

    val adaGagal  = riwayat.TotalGerakanGagal > 0
    val badgeColor = if (adaGagal) MerahMuda else HijauMuda
    val badgeText  = if (adaGagal) MerahUtama else HijauGelap

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Baris atas: nama sholat + badge status
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = riwayat.namaSholat,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 17.sp,
                    color      = HijauGelap
                )
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text     = if (adaGagal) "${riwayat.TotalGerakanGagal} Gagal" else "Sempurna",
                        color    = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(Modifier.height(10.dp))

            // Baris bawah: ikon status + tanggal + durasi
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikon status kecil
                Icon(
                    imageVector        = if (adaGagal) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier           = Modifier.size(14.dp),
                    tint               = badgeText
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text     = if (adaGagal) "Perlu perbaikan" else "Gerakan sempurna",
                    fontSize = 12.sp,
                    color    = badgeText
                )

                Spacer(Modifier.weight(1f))

                // Durasi
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier           = Modifier.size(12.dp),
                        tint               = Color.Gray
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(formatDurasi, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(6.dp))

            // Tanggal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier           = Modifier.size(12.dp),
                    tint               = Color.Gray
                )
                Spacer(Modifier.width(4.dp))
                Text(riwayat.tanggal, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────
@Preview(showBackground = true, device = Devices.PIXEL_4, showSystemUi = true)
@Composable
private fun RiwayatSiswaContentPreview() {
    val dummy = listOf(
        GuruViewModel.DataRiwayat(
            id = "1", namaSholat = "Sholat Subuh", tanggal = "13 Apr 2026, 04:30",
            durasiMillis = 180_000L, timestamp = 3L,
            TotalGerakanGagal = 2, GerakanGagal = "Rakaat 1:\n1. Ruku'\n2. Sujud Pertama"
        ),
        GuruViewModel.DataRiwayat(
            id = "2", namaSholat = "Sholat Dzuhur", tanggal = "12 Apr 2026, 12:15",
            durasiMillis = 245_000L, timestamp = 2L,
            TotalGerakanGagal = 0, GerakanGagal = ""
        ),
        GuruViewModel.DataRiwayat(
            id = "3", namaSholat = "Sholat Maghrib", tanggal = "11 Apr 2026, 18:05",
            durasiMillis = 210_000L, timestamp = 1L,
            TotalGerakanGagal = 1, GerakanGagal = "Tasyahud Akhir"
        )
    )
    Surface {
        RiwayatSiswaContent(
            namaSiswaTerpilih = "Bagas",
            isRiwayatLoading  = false,
            daftarRiwayat     = dummy,
            onBackClick       = {},
            onItemClick       = {}
        )
    }
}