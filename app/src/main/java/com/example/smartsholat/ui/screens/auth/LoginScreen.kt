package com.example.smartsholat.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartsholat.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.example.smartsholat.R

@Composable
fun LoginScreen(
    onNavigateToGuruHome: () -> Unit,
    onNavigateToSiswaHome: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Background abu muda agar konsisten
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Smart Sholat",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C) // Hijau identitas aplikasi
        )
        Text(
            text = "Assalamu'alaikum, silakan masuk",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp, top = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Email Guru / NIS Siswa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password / PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            errorMessage = "Harap isi kolom username dan password!"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = ""

                        val auth = FirebaseAuth.getInstance()
                        val db = FirebaseFirestore.getInstance()

                        fun fetchUserDataAndLogin(uid: String) {
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val role = document.getString("role") ?: "siswa"
                                        val nama = document.getString("nama") ?: "User"

                                        // --- PERBAIKAN DI SINI ---
                                        // 1. Ambil data NIS dari Firestore (jika guru, isikan "-" atau biarkan kosong)
                                        val nis = document.getString("nis") ?: "-"

                                        // 2. Simpan ke SessionManager dengan 4 parameter sesuai yang baru kita buat
                                        sessionManager.saveSession(uid, role, nama, nis)
                                        // -------------------------

                                        isLoading = false
                                        if (role == "guru") onNavigateToGuruHome() else onNavigateToSiswaHome()
                                    } else {
                                        isLoading = false
                                        errorMessage = "Data profil tidak ditemukan."
                                    }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = "Gagal mengambil data profil."
                                }
                        }

                        if (username.contains("@")) {
                            auth.signInWithEmailAndPassword(username, password)
                                .addOnSuccessListener { authResult ->
                                    fetchUserDataAndLogin(authResult.user?.uid ?: "")
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    errorMessage = "Email atau Password Guru salah!"
                                }
                        } else {
                            db.collection("users").whereEqualTo("nis", username).get()
                                .addOnSuccessListener { docs ->
                                    if (!docs.isEmpty) {
                                        val siswaDoc = docs.documents[0]
                                        val email = siswaDoc.getString("email") ?: ""
                                        val pinDariFirestore = siswaDoc.getString("pin") ?: ""
                                        if (password == pinDariFirestore) {
                                            auth.signInWithEmailAndPassword(email, password)
                                                .addOnSuccessListener { authResult ->
                                                    fetchUserDataAndLogin(authResult.user?.uid ?: "")
                                                }
                                                .addOnFailureListener {
                                                    isLoading = false
                                                    errorMessage = "Auth Gagal."
                                                }
                                        } else {
                                            isLoading = false
                                            errorMessage = "PIN salah!"
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = "NIS tidak terdaftar."
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Footer
        Text(
            text = "Developed by:\nBagas Cahyawiguna \u00A9 2026",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.logo_slbfkom),
            contentDescription = "Logo FKOM Uniku dan SLB",
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(
    showBackground = true,
    device = Devices.PIXEL_4,
    showSystemUi = true,
    name = "Login Screen Preview"
)
@Composable
fun LoginScreenPreview() {
    Surface {
        LoginScreen(
            onNavigateToGuruHome = {},
            onNavigateToSiswaHome = {}
        )
    }
}