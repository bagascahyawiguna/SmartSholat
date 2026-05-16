package com.example.smartsholat.ui.screens.guru

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

// Model Data Kelas
data class KelasInfo(val namaKelas: String, val jumlahSiswa: Int)

// Model Data Siswa (Sesuai field di Firestore)
data class DataSiswa(val id: String, val nama: String, val nis: String, val kelas: String)

class GuruViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // State untuk Dashboard Home
    var totalSiswa by mutableStateOf(0)
        private set
    var totalKelas by mutableStateOf(0)
        private set
    var totalEvaluasi by mutableStateOf(0)
        private set
    var daftarKelas = mutableStateListOf<KelasInfo>()
        private set
    var isLoading by mutableStateOf(true)
        private set

    // State untuk Daftar Siswa
    var daftarSiswa = mutableStateListOf<DataSiswa>()
        private set
    var isSiswaLoading by mutableStateOf(false)
        private set

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        isLoading = true
        isSiswaLoading = true

        db.collection("users")
            .whereEqualTo("role", "siswa")
            .get()
            .addOnSuccessListener { documents ->
                totalSiswa = documents.size()
                val mapKelas = mutableMapOf<String, Int>()

                daftarSiswa.clear()

                for (doc in documents) {
                    val kelas = doc.getString("kelas") ?: "Tanpa Kelas"
                    mapKelas[kelas] = mapKelas.getOrDefault(kelas, 0) + 1

                    val id = doc.getString("id") ?: doc.id
                    val nama = doc.getString("nama") ?: "Tanpa Nama"
                    val nis = doc.getString("nis") ?: "-"
                    val kelasSiswa = doc.getString("kelas") ?: "-"

                    daftarSiswa.add(DataSiswa(id, nama, nis, kelasSiswa))
                }

                totalKelas = mapKelas.size
                daftarKelas.clear()
                mapKelas.forEach { (namaKelas, jumlah) ->
                    daftarKelas.add(KelasInfo(namaKelas, jumlah))
                }

                isSiswaLoading = false

                db.collection("riwayat_evaluasi").get()
                    .addOnSuccessListener { riwayatDocs ->
                        totalEvaluasi = riwayatDocs.size()
                        isLoading = false
                    }
                    .addOnFailureListener { isLoading = false }
            }
            .addOnFailureListener {
                isLoading = false
                isSiswaLoading = false
            }
    }

    fun fetchSiswaByKelas(namaKelas: String) {
        isSiswaLoading = true
        daftarSiswa.clear()

        db.collection("users")
            .whereEqualTo("role", "siswa")
            .whereEqualTo("kelas", namaKelas)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val id = doc.getString("id") ?: doc.id
                    val nama = doc.getString("nama") ?: "Tanpa Nama"
                    val nis = doc.getString("nis") ?: "-"
                    val kelas = doc.getString("kelas") ?: "-"
                    daftarSiswa.add(DataSiswa(id, nama, nis, kelas))
                }
                isSiswaLoading = false
            }
            .addOnFailureListener { isSiswaLoading = false }
    }

    // ─────────────────────────────────────────────────────────
    // MODEL & STATE RIWAYAT
    // ─────────────────────────────────────────────────────────

    /**
     * DataRiwayat kini selaras dengan RiwayatItem milik siswa:
     * - TotalGerakanGagal : Int  → jumlah gerakan yang gagal
     * - GerakanGagal      : String → daftar nama gerakan gagal
     *   (bisa berisi "\n" sebagai pemisah rakaat, atau koma untuk data lama)
     */
    data class DataRiwayat(
        val id: String = "",
        val namaSholat: String = "",
        val tanggal: String = "",
        val durasiMillis: Long = 0L,
        val timestamp: Long = 0L,
        val TotalGerakanGagal: Int = 0,
        val GerakanGagal: String = ""
    )

    var namaSiswaTerpilih by mutableStateOf("")
        private set
    var daftarRiwayat = mutableStateListOf<DataRiwayat>()
        private set
    var isRiwayatLoading by mutableStateOf(false)
        private set

    // State untuk detail riwayat yang dipilih guru
    var riwayatTerpilih by mutableStateOf<DataRiwayat?>(null)
        private set

    fun pilihRiwayat(riwayat: DataRiwayat) {
        riwayatTerpilih = riwayat
    }

    fun clearRiwayatTerpilih() {
        riwayatTerpilih = null
    }

    fun fetchRiwayatSiswa(siswaId: String) {
        isRiwayatLoading = true
        daftarRiwayat.clear()
        namaSiswaTerpilih = "Memuat nama..."

        // 1. Ambil nama siswa
        db.collection("users").document(siswaId).get()
            .addOnSuccessListener { doc ->
                namaSiswaTerpilih = if (doc.exists())
                    doc.getString("nama") ?: "Siswa Tanpa Nama"
                else
                    "Data Siswa Tidak Ditemukan"
            }

        // 2. Ambil riwayat evaluasi — baca semua field termasuk gerakan gagal
        db.collection("riwayat_evaluasi")
            .whereEqualTo("siswaId", siswaId)
            .get()
            .addOnSuccessListener { documents ->
                val listSementara = mutableListOf<DataRiwayat>()
                for (doc in documents) {
                    listSementara.add(
                        DataRiwayat(
                            id                = doc.getString("id") ?: doc.id,
                            namaSholat        = doc.getString("namaSholat") ?: "Sholat Tidak Diketahui",
                            tanggal           = doc.getString("tanggal") ?: "-",
                            durasiMillis      = doc.getLong("durasiMillis") ?: 0L,
                            timestamp         = doc.getLong("timestamp") ?: 0L,
                            TotalGerakanGagal = (doc.getLong("TotalGerakanGagal") ?: 0L).toInt(),
                            GerakanGagal      = doc.getString("GerakanGagal") ?: ""
                        )
                    )
                }
                listSementara.sortByDescending { it.timestamp }
                daftarRiwayat.addAll(listSementara)
                isRiwayatLoading = false
            }
            .addOnFailureListener { isRiwayatLoading = false }
    }

    // ─────────────────────────────────────────────────────────
    // CRUD AKUN SISWA
    // ─────────────────────────────────────────────────────────

    fun tambahAkunSiswa(
        nama: String,
        nis: String,
        pin: String,
        kelas: String,
        onResult: (Boolean) -> Unit
    ) {
        val emailFiktif = "$nis@siswa.sholat.id"
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(emailFiktif, pin)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val dataBaru = hashMapOf(
                    "id"    to uid,
                    "nama"  to nama,
                    "nis"   to nis,
                    "pin"   to pin,
                    "kelas" to kelas,
                    "email" to emailFiktif,
                    "role"  to "siswa"
                )
                db.collection("users").document(uid).set(dataBaru)
                    .addOnSuccessListener { fetchDashboardData(); onResult(true) }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }

    fun hapusAkunSiswa(siswaId: String, onResult: (Boolean) -> Unit) {
        // Langkah 1: Cari semua riwayat evaluasi milik siswa ini
        db.collection("riwayat_evaluasi")
            .whereEqualTo("siswaId", siswaId)
            .get()
            .addOnSuccessListener { riwayatDocs ->
                // Langkah 2: Hapus semua riwayat menggunakan WriteBatch
                val batch = db.batch()
                for (doc in riwayatDocs) {
                    batch.delete(doc.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        // Langkah 3: Setelah riwayat bersih, hapus dokumen user
                        db.collection("users").document(siswaId).delete()
                            .addOnSuccessListener { fetchDashboardData(); onResult(true) }
                            .addOnFailureListener { onResult(false) }
                    }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }

    fun ubahAkunSiswa(
        siswaId: String,
        namaBaru: String,
        nisBaru: String,
        kelasBaru: String,
        onResult: (Boolean) -> Unit
    ) {
        val dataUpdate = mapOf(
            "nama"  to namaBaru,
            "nis"   to nisBaru,
            "kelas" to kelasBaru
        )
        db.collection("users").document(siswaId).update(dataUpdate)
            .addOnSuccessListener { fetchDashboardData(); onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}