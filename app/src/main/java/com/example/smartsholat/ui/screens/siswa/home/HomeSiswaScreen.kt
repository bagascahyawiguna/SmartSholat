package com.example.smartsholat.ui.screens.siswa.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsholat.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.smartsholat.R

// ==========================================
// 1. FUNGSI LOGIKA
// ==========================================
@Composable
fun HomeSiswaScreen(
    sessionManager: SessionManager,
    onBelajarClick: () -> Unit,
    onEvaluasiClick: () -> Unit,
    onModulLainnyaClick: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val userId = sessionManager.getUserId() ?: ""
    val todayPrefix = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    var completedPrayersCount by remember { mutableIntStateOf(0) }

    // FIX: Gunakan DisposableEffect agar listener Firestore di-cancel
    // saat composable meninggalkan komposisi (mencegah memory leak)
    DisposableEffect(userId) {
        if (userId.isEmpty()) return@DisposableEffect onDispose {}

        val listener = db.collection("riwayat_evaluasi")
            .whereEqualTo("siswaId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val uniquePrayers = snapshot.documents.mapNotNull { doc ->
                        val tgl = doc.getString("tanggal") ?: ""
                        if (tgl.startsWith(todayPrefix)) doc.getString("namaSholat") else null
                    }.distinct()

                    completedPrayersCount = uniquePrayers.size.coerceAtMost(5)
                }
            }

        onDispose { listener.remove() }
    }

    val namaLengkap = sessionManager.getNama() ?: "Siswa"
    val namaPanggilan = namaLengkap.split(" ")[0]

    HomeSiswaContent(
        namaPanggilan = namaPanggilan,
        completedPrayersCount = completedPrayersCount,
        onBelajarClick = onBelajarClick,
        onEvaluasiClick = onEvaluasiClick,
        onModulLainnyaClick = onModulLainnyaClick
    )
}

// ==========================================
// 2. FUNGSI TAMPILAN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSiswaContent(
    namaPanggilan: String,
    completedPrayersCount: Int,
    onBelajarClick: () -> Unit,
    onEvaluasiClick: () -> Unit,
    onModulLainnyaClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // ===== HEADER FLAT (tanpa rounded) =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF388E3C))
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 20.dp)
        ) {
            Column {
                Text(
                    text = "Assalamu'alaikum,",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                Text(
                    text = namaPanggilan,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ===== AREA KONTEN dengan LINGKARAN DEKORATIF =====
        // clipToBounds() memastikan setengah atas lingkaran yang "meluap" ke atas
        // akan ter-clip rapi, sehingga hanya setengah bawah yang terlihat.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            // LAYER 1: Kotak rounded hijau di belakang (hanya ±1/3 bawah yang terlihat)
            Image(
                painter = painterResource(id = R.drawable.bg_rounded_green),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,     // ← ganti dari FillWidth
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)                         // ← tinggi fix (tidak perlu aspectRatio)
                    .align(Alignment.TopCenter)
                    .offset(y = (-100).dp)                  // ← sembunyikan 100 dari 150 = 2/3 tersembunyi
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Target Harian", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Evaluasi 5 Waktu Sholat", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            val message = when {
                                completedPrayersCount == 0 -> "Ayo mulai evaluasi pertamamu hari ini!"
                                completedPrayersCount < 5 -> "Bagus! Teruskan evaluasi sholatmu hari ini."
                                else -> "Alhamdulillah! Kamu sudah menyelesaikan target hari ini."
                            }
                            Text(text = message, fontSize = 13.sp, color = Color.Gray)
                        }
                        Surface(
                            color = if (completedPrayersCount >= 5) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$completedPrayersCount / 5",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = if (completedPrayersCount >= 5) Color(0xFF388E3C) else Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                MenuUtamaCard(
                    title = "Belajar Bacaan",
                    subtitle = "Pelajari niat, doa, dan tata cara bacaan sholat.",
                    icon = Icons.Default.Book,
                    iconColor = Color(0xFF4CAF50),
                    onClick = onBelajarClick
                )

                MenuUtamaCard(
                    title = "Evaluasi Gerakan",
                    subtitle = "Evaluasi Gerakan Sholat Berbasis AI.",
                    icon = Icons.Default.CameraAlt,
                    iconColor = Color(0xFFE64A19),
                    onClick = onEvaluasiClick
                )

                MenuUtamaCard(
                    title = "Modul Lainnya",
                    subtitle = "Materi tambahan dari guru",
                    icon = Icons.AutoMirrored.Filled.LibraryBooks,
                    iconColor = Color(0xFF1565C0),
                    onClick = onModulLainnyaClick
                )
            }
        }
    }
}

@Composable
fun MenuUtamaCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
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
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// ==========================================
// 3. PREVIEW (tidak berubah logikanya)
// ==========================================
@Preview(showBackground = true, device = Devices.PIXEL_4, showSystemUi = true, name = "Home dengan Bottom Bar")
@Composable
fun HomeSiswaLengkapPreview() {
    Surface {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF388E3C),
                    contentColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF388E3C)) },
                        label = { Text("Home", fontSize = 12.sp) },
                        selected = true,
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White),
                        onClick = {}
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "Riwayat", tint = Color.White) },
                        label = { Text("Riwayat", fontSize = 12.sp) },
                        selected = false,
                        colors = NavigationBarItemDefaults.colors(unselectedTextColor = Color.White),
                        onClick = {}
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color.White) },
                        label = { Text("Profil", fontSize = 12.sp) },
                        selected = false,
                        colors = NavigationBarItemDefaults.colors(unselectedTextColor = Color.White),
                        onClick = {}
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                HomeSiswaContent(
                    namaPanggilan = "Bagas",
                    completedPrayersCount = 3,
                    onBelajarClick = {},
                    onEvaluasiClick = {}
                )
            }
        }
    }
}