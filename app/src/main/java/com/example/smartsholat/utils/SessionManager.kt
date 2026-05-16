package com.example.smartsholat.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class SessionManager(context: Context) {
    // Membuat file SharedPreferences khusus untuk aplikasi ini
    private val prefs: SharedPreferences = context.getSharedPreferences("smart_sholat_session", Context.MODE_PRIVATE)

    // PERUBAHAN 1: Tambahkan parameter 'nis' di sini
    fun saveSession(userId: String, role: String, nama: String, nis: String) {
        prefs.edit().apply {
            putBoolean("IS_LOGGED_IN", true)
            putString("USER_ID", userId)
            putString("ROLE", role)
            putString("NAMA", nama)
            putString("NIS", nis) // PERUBAHAN 2: Simpan NIS ke SharedPreferences
            apply() // apply() menyimpan data secara asinkron di latar belakang
        }
    }

    // Fungsi untuk mengecek apakah user sedang login
    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)

    // Fungsi untuk mengambil data yang tersimpan
    fun getRole(): String? = prefs.getString("ROLE", null)
    fun getUserId(): String? = prefs.getString("USER_ID", null)
    fun getNama(): String? = prefs.getString("NAMA", null)

    // PERUBAHAN 3: Fungsi untuk mengambil NIS
    fun getNis(): String? = prefs.getString("NIS", null)

    // Fungsi untuk menghapus data saat logout
    fun clearSession() {
        prefs.edit().clear().apply()
        FirebaseAuth.getInstance().signOut()
    }
}