package com.example.smartsholat.ui.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsholat.R
import com.example.smartsholat.utils.SessionManager
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToGuruHome: () -> Unit,
    onNavigateToSiswaHome: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    LaunchedEffect(key1 = true) {
        delay(2000L)

        if (sessionManager.isLoggedIn()) {
            if (sessionManager.getRole() == "guru") {
                onNavigateToGuruHome()
            } else {
                onNavigateToSiswaHome()
            }
        } else {
            onNavigateToLogin()
        }
    }

    // Menggunakan Box dengan gradasi hijau
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50), // Hijau Terang
                        Color(0xFF2E7D32)  // Hijau Gelap
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- LOGO APLIKASI ---
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)) // Efek glassmorphism tipis
                    .padding(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo), // Pastikan file bernama logo.jpeg/png
                    contentDescription = "Logo Smart Sholat",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- NAMA APLIKASI ---
            Text(
                text = "Smart Sholat",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Belajar Sholat dengan AI",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Image(
            painter = painterResource(id = R.drawable.slbxfkom_v2),
            contentDescription = "Logo SLB X FKOM",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .height(80.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    showSystemUi = true,
    name = "Splash Screen Preview"
)
@Composable
fun SplashScreenPreview() {
    // Membungkus dengan Surface (opsional, tapi baik untuk standar Compose)
    Surface {
        SplashScreen(
            onNavigateToLogin = {},     // Dikosongkan karena ini hanya untuk tampilan (UI)
            onNavigateToGuruHome = {},  // Tidak ada aksi navigasi yang benar-benar dijalankan
            onNavigateToSiswaHome = {}
        )
    }
}