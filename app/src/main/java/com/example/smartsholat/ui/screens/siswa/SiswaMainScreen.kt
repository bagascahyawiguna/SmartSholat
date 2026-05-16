package com.example.smartsholat.ui.screens.siswa

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartsholat.ui.navigation.Screen
import com.example.smartsholat.ui.screens.siswa.home.HomeSiswaScreen
import com.example.smartsholat.ui.screens.siswa.home.belajargerakan.BelajarSholatScreen
import com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan.EvaluasiSholatScreen
import com.example.smartsholat.ui.screens.siswa.profile.ProfilSiswaScreen
import com.example.smartsholat.ui.screens.siswa.riwayat.DetailRiwayatScreen
import com.example.smartsholat.ui.screens.siswa.riwayat.RiwayatItem
import com.example.smartsholat.ui.screens.siswa.riwayat.RiwayatSiswaScreen
import com.example.smartsholat.utils.SessionManager
import androidx.compose.foundation.layout.WindowInsets

@Composable
fun SiswaMainScreen(onLogoutClick: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // FIX #4: State holder untuk item riwayat yang dipilih.
    // Menggantikan URL-encoding yang rentan gagal jika teks GerakanGagal terlalu panjang.
    var riwayatTerpilih by remember { mutableStateOf<RiwayatItem?>(null) }

    val items = listOf(
        Pair("Home", Screen.HomeSiswa.route),
        Pair("Riwayat", Screen.RiwayatSiswa.route),
        Pair("Profil", Screen.ProfilSiswa.route)
    )

    // Pantau rute saat ini
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tentukan layar mana saja yang HARUS menyembunyikan bar sama sekali
    val shouldShowBottomBar = currentRoute?.let { route ->
        !route.startsWith(Screen.BelajarSholat.route) &&
                !route.startsWith("evaluasi_sholat_flow") &&
                !route.startsWith("detail_riwayat")
    } ?: true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF388E3C),
                    contentColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { (title, route) ->
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = when(title) {
                                        "Home" -> Icons.Default.Home
                                        "Riwayat" -> Icons.Default.History
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = title,
                                    tint = if (isSelected) Color(0xFF388E3C) else Color.White
                                )
                            },
                            label = { Text(title, fontSize = 12.sp) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedTextColor = Color.White,
                                unselectedIconColor = Color.White
                            ),
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val finalPadding = if (shouldShowBottomBar) innerPadding else PaddingValues(0.dp)

        NavHost(
            navController = navController,
            startDestination = Screen.HomeSiswa.route,
            modifier = Modifier.padding(finalPadding),
            enterTransition = {
                slideInHorizontally(animationSpec = tween(300)) { it / 4 } + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(300)) { -it / 4 } + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(animationSpec = tween(300)) { -it / 4 } + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = tween(300)) { it / 4 } + fadeOut(tween(300))
            }
        ) {
            // 1. HOME SISWA
            composable(Screen.HomeSiswa.route) {
                HomeSiswaScreen(
                    sessionManager = sessionManager,
                    onBelajarClick = { navController.navigate(Screen.BelajarSholat.route) },
                    onEvaluasiClick = { navController.navigate("evaluasi_sholat_flow") }
                )
            }

            // 2. BELAJAR SHOLAT
            composable(Screen.BelajarSholat.route) {
                BelajarSholatScreen { navController.popBackStack() }
            }

            // 3. ALUR EVALUASI SHOLAT (Menu -> Kamera -> Hasil)
            composable("evaluasi_sholat_flow") {
                EvaluasiSholatScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 4. RIWAYAT SISWA LIST
            composable(Screen.RiwayatSiswa.route) {
                RiwayatSiswaScreen(
                    sessionManager = sessionManager,
                    onRiwayatClick = { riwayat ->
                        // Simpan object langsung ke state, navigasi hanya dengan route sederhana
                        riwayatTerpilih = riwayat
                        navController.navigate("detail_riwayat")
                    }
                )
            }

            // 5. DETAIL RIWAYAT SISWA — baca dari state, bukan dari URL
            composable(route = "detail_riwayat") {
                val item = riwayatTerpilih
                if (item != null) {
                    DetailRiwayatScreen(
                        riwayat = item,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            // 6. PROFIL SISWA
            composable(Screen.ProfilSiswa.route) {
                ProfilSiswaScreen(
                    sessionManager = sessionManager,
                    onLogoutClick = onLogoutClick
                )
            }
        }
    }
}