package com.example.smartsholat.ui.screens.siswa.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsholat.R
import com.example.smartsholat.utils.SessionManager

@Composable
fun ProfilSiswaScreen(
    sessionManager: SessionManager,
    onLogoutClick: () -> Unit
) {
    val namaLengkap = sessionManager.getNama() ?: "Siswa SLB"
    val nisSiswa = sessionManager.getNis() ?: "-"

    ProfilSiswaContent(
        namaLengkap = namaLengkap,
        nisSiswa = nisSiswa,
        onLogoutClick = onLogoutClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilSiswaContent(
    namaLengkap: String,
    nisSiswa: String,
    onLogoutClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // MENGGUNAKAN SCAFFOLD & TOPAPPBAR AGAR STATUS BAR RAPI
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),  // ← tambahkan ini
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profil Siswa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF388E3C),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues) // Padding otomatis dari Scaffold
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Card Identitas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Foto Profil", modifier = Modifier.size(55.dp), tint = Color(0xFF388E3C))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = namaLengkap, modifier = Modifier.fillMaxWidth(), fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.Black)
                    Text(text = "NIS: $nisSiswa", modifier = Modifier.fillMaxWidth().padding(top = 4.dp), fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card Tentang Aplikasi
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF388E3C))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Tentang Aplikasi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Aplikasi Pembelajaran Interaktif dan Evaluasi Gerakan Sholat menggunakan AI untuk memfasilitasi kemandirian siswa disabilitas.", fontSize = 13.sp, lineHeight = 20.sp, color = Color.DarkGray, textAlign = TextAlign.Justify)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Versi: 2.0.0", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Logout
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar (Logout)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text("Developed by:\nBagas Cahyawiguna \u00A9 2026", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Image(painter = painterResource(id = R.drawable.logo_slbfkom), contentDescription = "Logo FKOM Uniku dan SLB", modifier = Modifier.fillMaxWidth().height(50.dp), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4, showSystemUi = true)
@Composable
fun ProfilSiswaLengkapPreview() {
    Surface {
        ProfilSiswaContent(namaLengkap = "Bagas Cahyawiguna", nisSiswa = "202201010", onLogoutClick = {})
    }
}