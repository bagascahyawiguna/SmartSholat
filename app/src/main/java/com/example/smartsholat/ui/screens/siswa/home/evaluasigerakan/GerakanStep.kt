package com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan

import com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan.PoseClassifier.PoseLabel

//GerakanStep — satu langkah dalam urutan evaluasi sholat.

data class GerakanStep(
    val targetLabel: PoseLabel,
    val namaGerakan: String,
    val feedbackText: String,
    val durasiDeteksiMs: Long = 5000L,
    val imageRes: Int // <--- TAMBAHAN BARU
)