package com.example.smartsholat.ui.screens.guru

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartsholat.ui.navigation.Screen
import com.example.smartsholat.ui.screens.guru.akun.ManajemenAkunScreen
import com.example.smartsholat.ui.screens.guru.home.DaftarSiswaScreen
import com.example.smartsholat.ui.screens.guru.home.DetailRiwayatGuruScreen
import com.example.smartsholat.ui.screens.guru.home.HomeGuruScreen
import com.example.smartsholat.ui.screens.guru.home.RiwayatSiswaScreen
import com.example.smartsholat.ui.screens.guru.modul.InputModulScreen
import com.example.smartsholat.ui.screens.guru.modul.TambahModulAjarScreen
import com.example.smartsholat.ui.screens.guru.profile.ProfilGuruScreen
import com.example.smartsholat.ui.screens.siswa.modul.DetailModulDinamisScreen

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun GuruMainScreen(
    onLogoutClick: () -> Unit
) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem("Home", Screen.HomeGuru.route, Icons.Default.Home),
        BottomNavItem("Akun", Screen.ManajemenAkun.route, Icons.Default.AccountBox),
        BottomNavItem("Profile", Screen.ProfilGuru.route, Icons.Default.Person)
    )

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tentukan route mana yang ingin menyembunyikan BottomNavigation
    val hideBottomBarRoutes = listOf(
        Screen.DaftarSiswa.route,
        Screen.RiwayatSiswaGuru.route,
        "detail_riwayat_guru/{riwayatId}", // PERBAIKAN: Sembunyikan bar di layar detail
        "tambah_modul_ajar",
        "input_modul?moduleId={moduleId}",
        "detail_modul_dinamis/{moduleId}"
    )

    val shouldShowBottomBar = currentRoute !in hideBottomBarRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // ← tambahkan ini
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = Color(0xFF388E3C)
                ) {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF388E3C),
                                selectedTextColor = Color(0xFF388E3C),
                                indicatorColor = Color(0xFFE8F5E9),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            onClick = {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
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
        val guruViewModel: GuruViewModel = viewModel()

        NavHost(
            navController = bottomNavController,
            startDestination = Screen.HomeGuru.route,
            modifier = Modifier.padding(if (shouldShowBottomBar) innerPadding else PaddingValues(0.dp)),
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
            composable(Screen.HomeGuru.route) {
                HomeGuruScreen(
                    viewModel = guruViewModel,
                    onKelasClick = { kelas ->
                        bottomNavController.navigate(Screen.DaftarSiswa.createRoute(kelas))
                    },
                    onTambahModulClick = {
                        bottomNavController.navigate("tambah_modul_ajar")
                    }
                )
            }

            composable(Screen.DaftarSiswa.route) { backStackEntry ->
                val namaKelas = backStackEntry.arguments?.getString("kelasId")
                DaftarSiswaScreen(
                    namaKelas = namaKelas,
                    viewModel = guruViewModel,
                    onBackClick = { bottomNavController.popBackStack() },
                    onSiswaClick = { siswaId ->
                        bottomNavController.navigate(Screen.RiwayatSiswaGuru.createRoute(siswaId))
                    }
                )
            }

            composable(Screen.RiwayatSiswaGuru.route) { backStackEntry ->
                val siswaId = backStackEntry.arguments?.getString("siswaId")
                RiwayatSiswaScreen(
                    siswaId = siswaId,
                    viewModel = guruViewModel,
                    onBackClick = {
                        bottomNavController.popBackStack()
                    },
                    // PERBAIKAN: Tangkap aksi klik dan kirim ID ke layar detail
                    onItemClick = { riwayat ->
                        bottomNavController.navigate("detail_riwayat_guru/${riwayat.id}")
                    }
                )
            }

            // PERBAIKAN: Rute Baru untuk Detail Riwayat Guru
            composable(
                route = "detail_riwayat_guru/{riwayatId}",
                arguments = listOf(navArgument("riwayatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val riwayatId = backStackEntry.arguments?.getString("riwayatId")

                // Cari data riwayat yang cocok di list ViewModel
                val riwayatDipilih = guruViewModel.daftarRiwayat.find { it.id == riwayatId }

                if (riwayatDipilih != null) {
                    DetailRiwayatGuruScreen(
                        riwayat = riwayatDipilih,
                        namaSiswa = guruViewModel.namaSiswaTerpilih,
                        onBackClick = { bottomNavController.popBackStack() }
                    )
                } else {
                    // Data tidak ditemukan (state hilang) — kembali otomatis
                    LaunchedEffect(Unit) { bottomNavController.popBackStack() }
                }
            }

            composable(Screen.ManajemenAkun.route) {
                ManajemenAkunScreen()
            }

            composable(Screen.ProfilGuru.route) {
                ProfilGuruScreen(
                    onLogoutClick = onLogoutClick
                )
            }

            // ===== ROUTE BARU: MODUL AJAR =====
            composable("tambah_modul_ajar") {
                TambahModulAjarScreen(
                    onBackClick = { bottomNavController.popBackStack() },
                    onAddClick = { bottomNavController.navigate("input_modul") },
                    onEditClick = { moduleId ->
                        bottomNavController.navigate("input_modul?moduleId=$moduleId")
                    },
                    onModulClick = { moduleId ->
                        bottomNavController.navigate("detail_modul_dinamis/$moduleId")
                    }
                )
            }

            composable(
                route = "input_modul?moduleId={moduleId}",
                arguments = listOf(navArgument("moduleId") {
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                InputModulScreen(
                    moduleId = backStackEntry.arguments?.getString("moduleId"),
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            composable(
                route = "detail_modul_dinamis/{moduleId}",
                arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
            ) { backStackEntry ->
                DetailModulDinamisScreen(
                    moduleId = backStackEntry.arguments?.getString("moduleId") ?: "",
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }
        }
    }
}