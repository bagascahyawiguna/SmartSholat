package com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan

import com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan.PoseClassifier.PoseLabel
import com.example.smartsholat.R

object SholatConfig {

    // Rakaat Pertama (Lengkap dengan Niat & Takbir)
    private val rakaatPertama = listOf(
        GerakanStep(PoseLabel.BERDIRI_ITIDAL, "Berniat", "Luruskan tubuhmu, menghadap kiblat, dan berniatlah dalam hati.", 5000L, R.drawable.niat),
        GerakanStep(PoseLabel.TAKBIR, "Takbiratul Ihram", "Angkat kedua tanganmu sejajar dengan telinga.", 5000L, R.drawable.takbiratul_ihram),
        GerakanStep(PoseLabel.SEDEKAP, "Bersedekap", "Letakkan tangan kanan di atas tangan kiri, di antara pusar dan dada.", 5000L, R.drawable.sedekap_iftitah),
        GerakanStep(PoseLabel.RUKU, "Ruku'", "Bungkukkan badanmu, luruskan punggung, dan genggam lututmu.", 5000L, R.drawable.rukuk),
        GerakanStep(PoseLabel.BERDIRI_ITIDAL, "I'tidal", "Bangkitlah dari ruku, dan berdiri tegak kembali.", 5000L, R.drawable.niat),
        GerakanStep(PoseLabel.SUJUD, "Sujud Pertama", "Bersujudlah. Tempelkan dahi, hidung, dan telapak tanganmu ke lantai.", 5000L, R.drawable.sujud),
        GerakanStep(PoseLabel.TASYAHUD, "Duduk Di Antara Dua Sujud", "Bangkitlah dari sujud, dan duduklah dengan tenang.", 5000L, R.drawable.duduk_diantara_sujud),
        GerakanStep(PoseLabel.SUJUD, "Sujud Kedua", "Kembali bersujud dengan sempurna.", 5000L, R.drawable.sujud)
    )

    private val isiRakaat = listOf(
        GerakanStep(PoseLabel.SEDEKAP, "Bersedekap", "Berdiri kembali dan bersedekaplah.", 5000L, R.drawable.sedekap_iftitah),
        GerakanStep(PoseLabel.RUKU, "Ruku'", "Bungkukkan badanmu untuk ruku.", 5000L, R.drawable.rukuk),
        GerakanStep(PoseLabel.BERDIRI_ITIDAL, "I'tidal", "Bangkitlah dari ruku, berdiri tegak.", 5000L, R.drawable.niat),
        GerakanStep(PoseLabel.SUJUD, "Sujud Pertama", "Bersujudlah kembali.", 5000L, R.drawable.sujud),
        GerakanStep(PoseLabel.TASYAHUD, "Duduk Di Antara Dua Sujud", "Duduklah di antara dua sujud.", 5000L, R.drawable.duduk_diantara_sujud),
        GerakanStep(PoseLabel.SUJUD, "Sujud Kedua", "Lakukan sujud kedua.", 5000L, R.drawable.sujud)
    )

    private val tasyahudAwal = listOf(
        GerakanStep(PoseLabel.TASYAHUD, "Tasyahud Awal", "Duduklah untuk tasyahud awal, letakkan tangan di atas paha.", 5000L, R.drawable.tasyahud_tahiyat_awal)
    )

    private val penutupSholat = listOf(
        GerakanStep(PoseLabel.TASYAHUD, "Tasyahud Akhir", "Duduklah untuk tasyahud akhir.", 5000L, R.drawable.tasyahud_tahiyat_akhir),
        GerakanStep(PoseLabel.SALAM, "Salam", "Tengokkan kepalamu perlahan ke kanan, kemudian ke kiri.", 5000L, R.drawable.salam_kanan)
    )

    fun buildUrutan(jumlahRakaat: Int): Map<Int, List<GerakanStep>> {
        val result = mutableMapOf<Int, List<GerakanStep>>()
        when (jumlahRakaat) {
            2 -> { // Subuh
                result[1] = rakaatPertama
                result[2] = isiRakaat + penutupSholat
            }
            3 -> { // Maghrib
                result[1] = rakaatPertama
                result[2] = isiRakaat + tasyahudAwal
                result[3] = isiRakaat + penutupSholat
            }
            4 -> { // Dzuhur, Ashar, Isya
                result[1] = rakaatPertama
                result[2] = isiRakaat + tasyahudAwal
                result[3] = isiRakaat
                result[4] = isiRakaat + penutupSholat
            }
            else -> {
                result[1] = rakaatPertama
                result[2] = isiRakaat + penutupSholat
            }
        }
        return result
    }
}