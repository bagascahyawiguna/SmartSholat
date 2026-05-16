package com.example.smartsholat.ui.screens.siswa.home.belajargerakan

import com.example.smartsholat.R

data class JenisSholat(
    val nama: String,
    val jumlahRakaat: Int,
    val icon: Int
)

data class GerakanSholat(
    val urutan: Int,
    val rakaat: Int,
    val namaGerakan: String,
    val bacaanArab: String,
    val bacaanLatin: String,
    val arti: String,
    val imageRes: Int
)

object DataBelajar {
    val daftarSholat = listOf(
        JenisSholat("Subuh", 2, R.drawable.ic_subuh),
        JenisSholat("Dzuhur", 4, R.drawable.ic_dzuhur),
        JenisSholat("Ashar", 4, R.drawable.ic_ashar),
        JenisSholat("Maghrib", 3, R.drawable.ic_maghrib),
        JenisSholat("Isya", 4, R.drawable.ic_isya)
    )

    fun getDaftarGerakan(namaSholat: String): List<GerakanSholat> {
        val sholat = daftarSholat.find { it.nama == namaSholat }
        val jumlahRakaat = sholat?.jumlahRakaat ?: 2
        val listStep = mutableListOf<GerakanSholat>()
        var stepCounter = 1

        fun addStep(rakaat: Int, nama: String, arab: String, latin: String, arti: String, img: Int) {
            listStep.add(GerakanSholat(stepCounter++, rakaat, nama, arab, latin, arti, img))
        }

        fun tambahNiatDanTakbir() {
            val (niatArab, niatLatin, niatArti) = getNiatBySholat(namaSholat)
            addStep(1, "Niat Sholat $namaSholat", niatArab, niatLatin, niatArti, R.drawable.niat)
            addStep(1, "Takbiratul Ihram", BacaanSholat.TAKBIR_ARAB, BacaanSholat.TAKBIR_LATIN, BacaanSholat.TAKBIR_ARTI, R.drawable.takbiratul_ihram)
            addStep(1, "Doa Iftitah", BacaanSholat.IFTITAH_ARAB, BacaanSholat.IFTITAH_LATIN, BacaanSholat.IFTITAH_ARTI, R.drawable.sedekap_iftitah)
        }

        fun tambahBacaanInti(rakaat: Int, bacaSurah: Boolean = true) {
            addStep(rakaat, "Membaca Al-Fatihah", BacaanSholat.FATIHAH_ARAB, BacaanSholat.FATIHAH_LATIN, BacaanSholat.FATIHAH_ARTI, R.drawable.sedekap_iftitah)
            if (bacaSurah) {
                // LOGIKA BARU: Beda rakaat, beda surah
                if (rakaat == 1) {
                    addStep(rakaat, "Membaca Surah Pendek (Al-Ikhlas)", BacaanSholat.SURAH1_ARAB, BacaanSholat.SURAH1_LATIN, BacaanSholat.SURAH1_ARTI, R.drawable.sedekap_iftitah)
                } else if (rakaat == 2) {
                    addStep(rakaat, "Membaca Surah Pendek (Al-Kautsar)", BacaanSholat.SURAH2_ARAB, BacaanSholat.SURAH2_LATIN, BacaanSholat.SURAH2_ARTI, R.drawable.sedekap_iftitah)
                }
            }
        }

        fun tambahRangkaianRukuSujud(rakaat: Int) {
            addStep(rakaat, "Ruku'", BacaanSholat.RUKU_ARAB, BacaanSholat.RUKU_LATIN, BacaanSholat.RUKU_ARTI, R.drawable.rukuk)
            addStep(rakaat, "I'tidal", BacaanSholat.ITIDAL_ARAB, BacaanSholat.ITIDAL_LATIN, BacaanSholat.ITIDAL_ARTI, R.drawable.niat)
            addStep(rakaat, "Sujud Pertama", BacaanSholat.SUJUD_ARAB, BacaanSholat.SUJUD_LATIN, BacaanSholat.SUJUD_ARTI, R.drawable.sujud)
            addStep(rakaat, "Duduk Diantara Sujud", BacaanSholat.DUDUK_ARAB, BacaanSholat.DUDUK_LATIN, BacaanSholat.DUDUK_ARTI, R.drawable.duduk_diantara_sujud)
            addStep(rakaat, "Sujud Kedua", BacaanSholat.SUJUD_ARAB, BacaanSholat.SUJUD_LATIN, BacaanSholat.SUJUD_ARTI, R.drawable.sujud)
        }

        fun tambahTasyahud(rakaat: Int, isAkhir: Boolean) {
            val nama = if (isAkhir) "Tasyahud Akhir" else "Tasyahud Awal"
            val img = if (isAkhir) R.drawable.tasyahud_tahiyat_akhir else R.drawable.tasyahud_tahiyat_awal
            addStep(rakaat, nama, BacaanSholat.TASYAHUD_ARAB, BacaanSholat.TASYAHUD_LATIN, BacaanSholat.TASYAHUD_ARTI, img)
        }

        fun tambahSalam(rakaat: Int) {
            addStep(rakaat, "Salam ke Kanan", BacaanSholat.SALAM_ARAB, BacaanSholat.SALAM_LATIN, BacaanSholat.SALAM_ARTI, R.drawable.salam_kanan)
            addStep(rakaat, "Salam ke Kiri", BacaanSholat.SALAM_ARAB, BacaanSholat.SALAM_LATIN, BacaanSholat.SALAM_ARTI, R.drawable.salam_kiri)
        }

        // ================= MERAKIT SHOLAT =================
        for (r in 1..jumlahRakaat) {
            if (r == 1) {
                tambahNiatDanTakbir()
                tambahBacaanInti(r, bacaSurah = true)
                tambahRangkaianRukuSujud(r)
            } else {
                tambahBacaanInti(r, bacaSurah = r <= 2)
                tambahRangkaianRukuSujud(r)
            }

            if (jumlahRakaat == 2 && r == 2) {
                tambahTasyahud(r, isAkhir = true)
                tambahSalam(r)
            } else if ((jumlahRakaat == 3 || jumlahRakaat == 4) && r == 2) {
                tambahTasyahud(r, isAkhir = false)
            } else if (r == jumlahRakaat && jumlahRakaat > 2) {
                tambahTasyahud(r, isAkhir = true)
                tambahSalam(r)
            }
        }

        return listStep
    }

    private fun getNiatBySholat(nama: String): Triple<String, String, String> {
        return when (nama) {
            "Subuh" -> Triple("اُصَلِّى فَرْضَ الصُّبْحِ رَكْعَتَيْنِ مُسْتَقْبِلَ الْقِبْلَةِ اَدَاءً ِللهِ تَعَالَى", "Ushalli fardhash shubhi rak'ataini mustaqbilal qiblati adaa-an lillaahi ta'aalaa", "Aku niat melakukan sholat fardu Subuh dua rakaat, menghadap kiblat karena Allah Ta'ala.")
            "Dzuhur" -> Triple("اُصَلِّى فَرْضَ الظُّهْرِ اَرْبَعَ رَكَعَاتٍ مُسْتَقْبِلَ الْقِبْلَةِ اَدَاءً ِللهِ تَعَالَى", "Ushalli fardhadh dhuhri arba'a raka'aatin mustaqbilal qiblati adaa-an lillaahi ta'aalaa", "Aku niat melakukan sholat fardu Dzuhur empat rakaat, menghadap kiblat karena Allah Ta'ala.")
            "Ashar" -> Triple("اُصَلِّى فَرْضَ الْعَصْرِ اَرْبَعَ رَكَعَاتٍ مُسْتَقْبِلَ الْقِبْلَةِ اَدَاءً ِللهِ تَعَالَى", "Ushalli fardhal 'ashri arba'a raka'aatin mustaqbilal qiblati adaa-an lillaahi ta'aalaa", "Aku niat melakukan sholat fardu Ashar empat rakaat, menghadap kiblat karena Allah Ta'ala.")
            "Maghrib" -> Triple("اُصَلِّى فَرْضَ الْمَغْرِبِ ثَلاَثَ رَكَعَاتٍ مُسْتَقْبِلَ الْقِبْلَةِ اَدَاءً ِللهِ تَعَالَى", "Ushalli fardhal maghribi tsalaatsa raka'aatin mustaqbilal qiblati adaa-an lillaahi ta'aalaa", "Aku niat melakukan sholat fardu Maghrib tiga rakaat, menghadap kiblat karena Allah Ta'ala.")
            "Isya" -> Triple("اُصَلِّى فَرْضَ الْعِشَاءِ اَرْبَعَ رَكَعَاتٍ مُسْتَقْبِلَ الْقِبْلَةِ اَدَاءً ِللهِ تَعَالَى", "Ushalli fardhal 'isyaa-i arba'a raka'aatin mustaqbilal qiblati adaa-an lillaahi ta'aalaa", "Aku niat melakukan sholat fardu Isya empat rakaat, menghadap kiblat karena Allah Ta'ala.")
            else -> Triple("...", "...", "...")
        }
    }
}