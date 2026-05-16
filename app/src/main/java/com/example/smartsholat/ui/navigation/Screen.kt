package com.example.smartsholat.ui.navigation

sealed class Screen(val route: String) {
    // Shared
    object Splash : Screen("splash")
    object Login : Screen("login")

    // Rute Siswa
    object HomeSiswa : Screen("home_siswa")
    object BelajarSholat : Screen("belajar_sholat")
    object EvaluasiCamera : Screen("evaluasi_camera/{namaSholat}") {
        fun createRoute(namaSholat: String) = "evaluasi_camera/$namaSholat"
    }

    // --- UBAH BAGIAN INI ---
    object ResultEvaluasi : Screen("result_evaluasi/{namaSholat}/{durasi}/{totalGagal}/{gerakanGagal}") {
        fun createRoute(namaSholat: String, durasi: Long, totalGagal: Int, gerakanGagal: String): String {
            // Encode teks agar aman dibawa lewat URL Navigasi (mencegah crash jika ada spasi/kosong)
            val safeGerakan = if (gerakanGagal.isEmpty()) "Tidak_Ada" else android.net.Uri.encode(gerakanGagal)
            return "result_evaluasi/$namaSholat/$durasi/$totalGagal/$safeGerakan"
        }
    }
    // -----------------------

    object RiwayatSiswa : Screen("riwayat_siswa")
    object ProfilSiswa : Screen("profil_siswa")

    // Rute Guru
    object HomeGuru : Screen("home_guru")
    object ManajemenAkun : Screen("manajemen_akun")
    object ProfilGuru : Screen("profil_guru")

    object DaftarSiswa : Screen("daftar_siswa/{kelasId}") {
        fun createRoute(kelasId: String) = "daftar_siswa/$kelasId"
    }

    object RiwayatSiswaGuru : Screen("riwayat_siswa_guru/{siswaId}") {
        fun createRoute(siswaId: String) = "riwayat_siswa_guru/$siswaId"
    }
}