package com.example.smartsholat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartsholat.ui.screens.auth.LoginScreen
import com.example.smartsholat.ui.screens.shared.SplashScreen
import androidx.compose.ui.platform.LocalContext
import com.example.smartsholat.ui.screens.siswa.SiswaMainScreen
import com.example.smartsholat.utils.SessionManager

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Halaman Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                },
                onNavigateToGuruHome = {
                    navController.navigate(Screen.HomeGuru.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                },
                onNavigateToSiswaHome = {
                    navController.navigate(Screen.HomeSiswa.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                }
            )
        }

        // 2. Halaman Login
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToGuruHome = {
                    navController.navigate(Screen.HomeGuru.route) {
                        popUpTo(Screen.Login.route) { inclusive = true } // Hapus login dari backstack
                    }
                },
                onNavigateToSiswaHome = {
                    navController.navigate(Screen.HomeSiswa.route) {
                        popUpTo(Screen.Login.route) { inclusive = true } // Hapus login dari backstack
                    }
                }
            )
        }

        // 3. Halaman Utama Guru
        composable(Screen.HomeGuru.route) {
            com.example.smartsholat.ui.screens.guru.GuruMainScreen(
                onLogoutClick = {
                    // HAPUS SESI SAAT LOGOUT
                    sessionManager.clearSession()

                    // Kembali ke halaman Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.HomeGuru.route) { inclusive = true }
                    }
                }
            )
        }

        // 4. Halaman Utama Siswa (Ganti Box dummy dengan SiswaMainScreen)
        composable(Screen.HomeSiswa.route) {
            SiswaMainScreen(
                onLogoutClick = {
                    // Hapus sesi saat siswa logout
                    sessionManager.clearSession()

                    // Kembali ke login dan bersihkan backstack
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.HomeSiswa.route) { inclusive = true }
                    }
                }
            )
        }
    }
}