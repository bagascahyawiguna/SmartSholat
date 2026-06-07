package com.example.smartsholat.ui.screens.siswa.riwayat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsholat.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.annotation.Keep

// ── Warna Tema (Disamakan dengan Guru) ────────────────────────
private val HijauUtama  = Color(0xFF388E3C)
private val HijauGelap  = Color(0xFF2E7D32)
private val HijauMuda   = Color(0xFFE8F5E9)
private val MerahUtama  = Color(0xFFD32F2F)
private val MerahMuda   = Color(0xFFFFEBEE)
private val MerahBorder = Color(0xFFFFCDD2)
private val AbuBg       = Color(0xFFF5F5F5)

@Keep
data class RiwayatItem(
    val id: String = "",
    val siswaId: String = "",
    val namaSholat: String = "",
    val tanggal: String = "",
    val durasiMillis: Long = 0,
    val timestamp: Long = 0,
    val TotalGerakanGagal: Int = 0,
    val GerakanGagal: String = ""
)

// ─────────────────────────────────────────────────────────────
// 1. DAFTAR RIWAYAT SISWA
// ─────────────────────────────────────────────────────────────
@Composable
fun RiwayatSiswaScreen(
    sessionManager: SessionManager,
    onRiwayatClick: (RiwayatItem) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userId = sessionManager.getUserId() ?: ""

    var riwayatList by remember { mutableStateOf<List<RiwayatItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // FIX: Gunakan DisposableEffect agar listener Firestore di-cancel
    // saat composable meninggalkan komposisi (mencegah memory leak)
    DisposableEffect(userId) {
        if (userId.isEmpty()) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }
        val listener = db.collection("riwayat_evaluasi")
            .whereEqualTo("siswaId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Riwayat", "Listen failed.", e)
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    riwayatList = snapshot.toObjects(RiwayatItem::class.java)
                }
                isLoading = false
            }
        onDispose { listener.remove() }
    }

    RiwayatSiswaContent(
        riwayatList = riwayatList,
        isLoading = isLoading,
        onItemClick = onRiwayatClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatSiswaContent(
    riwayatList: List<RiwayatItem>,
    isLoading: Boolean,
    onItemClick: (RiwayatItem) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),  // ← tambahkan ini
        topBar = {
            TopAppBar(
                title = {
                    Text("Riwayat Evaluasi", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HijauUtama)
                }
            } else if (riwayatList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color.LightGray
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Belum ada riwayat evaluasi", color = Color.Gray, fontSize = 15.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(riwayatList) { item ->
                        RiwayatCard(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
private fun RiwayatSiswaContentPreview() {
    RiwayatSiswaContent(
        riwayatList = listOf(
            RiwayatItem(
                id = "1", namaSholat = "Sholat Subuh", tanggal = "07 Juni 2026",
                durasiMillis = 95000, TotalGerakanGagal = 0, GerakanGagal = ""
            ),
            RiwayatItem(
                id = "2", namaSholat = "Sholat Dzuhur", tanggal = "06 Juni 2026",
                durasiMillis = 180000, TotalGerakanGagal = 2,
                GerakanGagal = "Rukuk - posisi punggung kurang lurus, Sujud - posisi tangan salah"
            )
        ),
        isLoading = false,
        onItemClick = {}
    )
}

@Composable
fun RiwayatCard(item: RiwayatItem, onClick: () -> Unit) {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(item.durasiMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(item.durasiMillis) % 60
    val formatDurasi = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val adaGagal  = item.TotalGerakanGagal > 0
    val badgeColor = if (adaGagal) MerahMuda else HijauMuda
    val badgeText  = if (adaGagal) MerahUtama else HijauGelap

    // STYLE CARD DISAMAKAN DENGAN GURU
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.namaSholat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = HijauGelap
                )
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (adaGagal) "${item.TotalGerakanGagal} Gagal" else "Sempurna",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (adaGagal) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = badgeText
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (adaGagal) "Perlu perbaikan" else "Gerakan sempurna",
                    fontSize = 12.sp,
                    color = badgeText
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = formatDurasi, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = item.tanggal, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}