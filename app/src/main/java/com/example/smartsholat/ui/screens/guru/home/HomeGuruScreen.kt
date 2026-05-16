package com.example.smartsholat.ui.screens.guru.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsholat.ui.screens.guru.GuruViewModel
import com.example.smartsholat.ui.screens.guru.KelasInfo

// --- 1. FUNGSI LOGIKA (STATEFUL) ---

@Composable
fun HomeGuruScreen(
    viewModel: GuruViewModel,
    onKelasClick: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.fetchDashboardData()
    }

    HomeGuruContent(
        isLoading      = viewModel.isLoading,
        totalEvaluasi  = viewModel.totalEvaluasi,
        totalSiswa     = viewModel.totalSiswa,
        totalKelas     = viewModel.totalKelas,
        daftarKelas    = viewModel.daftarKelas,
        onKelasClick   = onKelasClick
    )
}

// --- 2. FUNGSI TAMPILAN (STATELESS) ---
@Composable
fun HomeGuruContent(
    isLoading     : Boolean,
    totalEvaluasi : Int,
    totalSiswa    : Int,
    totalKelas    : Int,
    daftarKelas   : List<KelasInfo>,
    onKelasClick  : (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ===== HEADER =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF388E3C))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 20.dp)
        ) {
            Column {
                Text(
                    text       = "Dashboard Monitoring",
                    color      = Color.White,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = "Pantau perkembangan ibadah siswa",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        // ===== KONTEN UTAMA =====
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier           = Modifier.padding(32.dp).fillMaxWidth(),
                        contentAlignment   = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF388E3C))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier        = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding  = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ===== 3 MONITORING CARDS =====
                item {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MonitoringCard(
                            title    = "Total Evaluasi",
                            value    = totalEvaluasi.toString(),
                            icon     = Icons.Default.AssignmentTurnedIn,
                            iconBg   = Color(0xFFE8F5E9),
                            iconTint = Color(0xFF388E3C),
                            modifier = Modifier.weight(1f)
                        )
                        MonitoringCard(
                            title    = "Total Siswa",
                            value    = totalSiswa.toString(),
                            icon     = Icons.Default.People,
                            iconBg   = Color(0xFFE3F2FD),
                            iconTint = Color(0xFF1976D2),
                            modifier = Modifier.weight(1f)
                        )
                        MonitoringCard(
                            title    = "Total Kelas",
                            value    = totalKelas.toString(),
                            icon     = Icons.Default.MeetingRoom,
                            iconBg   = Color(0xFFFFF3E0),
                            iconTint = Color(0xFFE65100),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ===== JUDUL DAFTAR KELAS =====
                item {
                    Text(
                        text       = "Daftar Kelas",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(top = 12.dp)
                    )
                }

                // ===== DAFTAR KELAS =====
                if (daftarKelas.isEmpty()) {
                    item {
                        Text("Belum ada data siswa dan kelas.", color = Color.Gray)
                    }
                } else {
                    // Natural sort: pisahkan token angka & teks agar "2A" < "10A"
                    val kelasTerurut = daftarKelas.sortedWith(Comparator { a, b ->
                        fun tokens(s: String): List<Any> {
                            val result = mutableListOf<Any>()
                            val regex = Regex("(\\d+)|(\\D+)")
                            regex.findAll(s.uppercase()).forEach { m ->
                                m.groupValues[1].toIntOrNull()
                                    ?.let { result.add(it) }
                                    ?: result.add(m.groupValues[2])
                            }
                            return result
                        }
                        val ta = tokens(a.namaKelas)
                        val tb = tokens(b.namaKelas)
                        val len = minOf(ta.size, tb.size)
                        for (i in 0 until len) {
                            val cmp = when {
                                ta[i] is Int && tb[i] is Int ->
                                    (ta[i] as Int).compareTo(tb[i] as Int)
                                else ->
                                    ta[i].toString().compareTo(tb[i].toString())
                            }
                            if (cmp != 0) return@Comparator cmp
                        }
                        ta.size.compareTo(tb.size)
                    })
                    items(kelasTerurut) { kelasInfo ->
                        KelasCard(
                            kelas   = kelasInfo,
                            onClick = { onKelasClick(kelasInfo.namaKelas) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// KOMPONEN: MONITORING CARD (Rounded Square Icon)
// ─────────────────────────────────────────────────────────
@Composable
fun MonitoringCard(
    title    : String,
    value    : String,
    icon     : ImageVector,
    iconBg   : Color,
    iconTint : Color,
    modifier : Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier              = Modifier
                .padding(14.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            // Ikon dengan rounded square background (mirip HomeSiswaScreen)
            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = iconBg,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector     = icon,
                    contentDescription = null,
                    tint            = iconTint,
                    modifier        = Modifier.padding(10.dp)
                )
            }

            Text(
                text       = value,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = iconTint
            )
            Text(
                text      = title,
                fontSize  = 10.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center,
                color     = Color.Gray
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// KOMPONEN: KELAS CARD
// ─────────────────────────────────────────────────────────
@Composable
fun KelasCard(kelas: KelasInfo, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier              = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(text = kelas.namaKelas, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                Text(
                    text     = "${kelas.jumlahSiswa} Siswa",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color    = Color(0xFF388E3C),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// PREVIEW 1 — Tampilan Penuh
// ─────────────────────────────────────────────────────────
@Preview(
    showBackground = true,
    device         = Devices.PIXEL_4,
    showSystemUi   = true,
    name           = "1 · Home Guru – Tampilan Penuh"
)
@Composable
fun HomeGuruLengkapPreview() {
    val dummyKelas = listOf(
        KelasInfo("Kelas A (Tunawicara)", 12),
        KelasInfo("Kelas B (Down Syndrome)", 8),
        KelasInfo("Kelas C (Autis)", 10)
    )

    Surface {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF388E3C),
                    contentColor   = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon     = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF388E3C)) },
                        label    = { Text("Home", fontSize = 12.sp) },
                        selected = true,
                        colors   = NavigationBarItemDefaults.colors(indicatorColor = Color.White),
                        onClick  = {}
                    )
                    NavigationBarItem(
                        icon     = { Icon(Icons.Default.People, contentDescription = "Siswa", tint = Color.White) },
                        label    = { Text("Siswa", fontSize = 12.sp) },
                        selected = false,
                        colors   = NavigationBarItemDefaults.colors(unselectedTextColor = Color.White),
                        onClick  = {}
                    )
                    NavigationBarItem(
                        icon     = { Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color.White) },
                        label    = { Text("Profil", fontSize = 12.sp) },
                        selected = false,
                        colors   = NavigationBarItemDefaults.colors(unselectedTextColor = Color.White),
                        onClick  = {}
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                HomeGuruContent(
                    isLoading     = false,
                    totalEvaluasi = 156,
                    totalSiswa    = 30,
                    totalKelas    = 3,
                    daftarKelas   = dummyKelas,
                    onKelasClick  = {}
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// PREVIEW 2 — Kondisi Loading
// ─────────────────────────────────────────────────────────
@Preview(
    showBackground = true,
    device         = Devices.PIXEL_4,
    showSystemUi   = true,
    name           = "2 · Home Guru – Loading"
)
@Composable
fun PreviewHomeGuruLoading() {
    Surface {
        Scaffold { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                HomeGuruContent(
                    isLoading     = true,
                    totalEvaluasi = 0,
                    totalSiswa    = 0,
                    totalKelas    = 0,
                    daftarKelas   = emptyList(),
                    onKelasClick  = {}
                )
            }
        }
    }
}