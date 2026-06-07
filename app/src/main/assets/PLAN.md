# PLAN.MD — Penambahan Fitur Modul Ajar Dinamis (CRUD)
## Proyek: SmartSholat — Tambahan Pasca Sidang Hasil

---

## ⚠️ ATURAN UTAMA — WAJIB DIPATUHI SEPANJANG IMPLEMENTASI

> **DILARANG KERAS mengubah, menghapus, memindahkan, atau mengurangi
> kode program yang sudah ada dalam kondisi apapun.**
>
> Yang diperbolehkan hanya:
> - Menambahkan baris/blok kode BARU pada file yang sudah ada,
>   ditempatkan SETELAH kode yang sudah ada (tidak menyisip di tengah)
> - Membuat file/class Kotlin BARU yang sepenuhnya terpisah
> - Menambahkan item navigasi BARU di nav graph yang sudah ada
> - Menambahkan dependency BARU di build.gradle (bukan mengganti)
>
> Jika ragu antara "ubah" dan "tambah", pilih membuat file baru.

---

## Ringkasan Fitur

Guru dapat membuat **Modul Ajar Dinamis** berbasis teks terstruktur
yang terdiri dari satu judul modul dan satu atau lebih langkah/deskripsi.
Setiap modul yang dibuat guru akan tampil sebagai card baru di halaman
**"Modul Lainnya"** milik siswa. Seluruh data disimpan ke **Firebase
Firestore** sebagai teks biasa — tidak ada file upload, tidak ada storage
berbayar, 100% dalam batas gratis Spark Plan Firebase.

---

## Database: Firebase Firestore (Sudah Terpasang)

Tidak ada konfigurasi Firebase baru. Cukup tambahkan collection baru
di Firestore yang sudah ada. Ambil Firebase UID guru dari:
`FirebaseAuth.getInstance().currentUser?.uid`

### Struktur Collection Baru

```
firestore/
├── users/          ← SUDAH ADA, TIDAK DISENTUH
├── evaluations/    ← SUDAH ADA, TIDAK DISENTUH
│
└── dynamic_modules/                     ← COLLECTION BARU
    └── {moduleId} (auto-generated)/
        ├── title         : String       — Judul modul, mis. "Tata Cara Wudhu"
        ├── description   : String       — Deskripsi singkat modul (opsional)
        ├── category      : String       — Kategori, mis. "Bersuci"
        ├── createdByUid  : String       — Firebase UID guru pembuat
        ├── createdByName : String       — Nama guru pembuat
        ├── isPublished   : Boolean      — true = tampil ke siswa
        ├── createdAt     : Timestamp    — Waktu dibuat
        ├── updatedAt     : Timestamp    — Waktu terakhir diubah
        │
        └── steps/  (subcollection)
            └── {stepId} (auto-generated)/
                ├── stepOrder       : Int     — Urutan langkah (1, 2, 3, ...)
                ├── title           : String  — Sub-judul langkah (WAJIB)
                ├── description     : String  — Penjelasan langkah (WAJIB)
                ├── arabicText      : String  — Bacaan Arab (opsional, boleh "")
                ├── latinText       : String  — Bacaan Latin (opsional, boleh "")
                └── translationText : String  — Arti bacaan (opsional, boleh "")
```

---

## Dependency Baru (Opsional — hanya jika belum ada)

Cek `build.gradle` app-level. Jika belum ada, tambahkan:

```gradle
// Tidak ada dependency baru yang wajib ditambahkan.
// Firebase Firestore sudah terpasang dari fitur sebelumnya.
// Seluruh fitur baru menggunakan dependency yang sudah ada.
```

---

## Daftar File yang Perlu Dimodifikasi dan Dibuat

### File yang sudah ada — HANYA TAMBAH kode baru di bagian bawah:

| File | Perubahan |
|------|-----------|
| `HomeGuruScreen.kt` | Tambah 1 card baru "Tambah Modul Ajar" di bawah 3 card yang ada |
| `HomeSiswaScreen.kt` | Tambah 1 MenuUtamaCard baru "Modul Lainnya" di bawah card "Evaluasi Gerakan" |
| Nav Graph / `NavHost` | Tambah route baru untuk 4 halaman baru |

### File baru yang harus dibuat:

```
app/src/main/java/com/example/smartsholat/
│
├── data/model/
│   ├── DynamicModule.kt        — data class untuk dokumen dynamic_modules
│   └── ModuleStep.kt           — data class untuk subcollection steps
│
├── data/repository/
│   └── DynamicModuleRepository.kt  — semua operasi CRUD ke Firestore
│
└── ui/screens/
    ├── guru/modul/
    │   ├── TambahModulAjarScreen.kt      — halaman daftar modul + FAB tambah
    │   ├── InputModulScreen.kt           — halaman form input modul baru
    │   └── ModulAjarViewModel.kt         — ViewModel untuk state manajemen
    │
    └── siswa/modul/
        ├── ModulLainnyaScreen.kt         — halaman daftar modul untuk siswa
        └── DetailModulDinamisScreen.kt   — halaman stepper detail modul
```

---

## Detail Implementasi Per Langkah

---

### LANGKAH 1 — Tambah Card di `HomeGuruScreen.kt`

**Aturan:** Tambahkan kode BARU di bawah 3 card yang sudah ada.
Jangan ubah atau sentuh card "Total Evaluasi", "Total Siswa", "Total Kelas".

**Tata letak yang diinginkan:**
```
[Total Evaluasi]  [Total Siswa]  [Total Kelas]   ← SUDAH ADA, TIDAK DIUBAH
[         Tambah Modul Ajar          ]            ← CARD BARU di bawahnya
```

**Spesifikasi card baru:**
- Teks: **"Tambah Modul Ajar"**
- Lebar: full width (modifier fillMaxWidth)
- Ikon: ikon buku atau tambah (sesuaikan dengan ikon yang sudah dipakai di app)
- Warna: sesuaikan dengan tema hijau aplikasi (#388E3C)
- Aksi klik: navigasi ke route `tambah_modul_ajar`

---

### LANGKAH 2 — `TambahModulAjarScreen.kt` (Halaman Daftar Modul Guru)

**Deskripsi:** Halaman ini menampilkan daftar modul yang sudah dibuat oleh
guru yang sedang login (filter berdasarkan `createdByUid`).

**Kondisi tampilan:**

**A. Jika belum ada modul:**
```
[AppBar: "Modul Ajar"]

        📭
   Belum ada modul yang
      ditambahkan

                          [+ FAB]
```

**B. Jika sudah ada modul:**
```
[AppBar: "Modul Ajar"]

┌─────────────────────────────┐
│ Tata Cara Wudhu             │  [✏️] [🗑️]
│ Bersuci • 5 langkah         │
└─────────────────────────────┘
┌─────────────────────────────┐
│ Tata Cara Tayamum           │  [✏️] [🗑️]
│ Bersuci • 3 langkah         │
└─────────────────────────────┘

                          [+ FAB]
```

**Elemen UI:**
- `TopAppBar` dengan judul "Modul Ajar" dan tombol back
- `LazyColumn` daftar card modul
- Setiap card modul berisi: judul, kategori, jumlah langkah
- Tiap card punya 2 tombol di samping kanan: **Edit (ikon pensil)** dan
  **Hapus (ikon tempat sampah)**
- `FloatingActionButton` di pojok kanan bawah dengan ikon "+" dan
  label "Tambah Modul"
- Klik card modul → navigasi ke `DetailModulDinamisScreen` (mode preview guru)
- Klik FAB → navigasi ke `InputModulScreen` (mode tambah baru)
- Klik Edit → navigasi ke `InputModulScreen` (mode edit, kirim moduleId)
- Klik Hapus → tampilkan **Dialog Konfirmasi Penghapusan** (lihat detail di bawah)

**Dialog Konfirmasi Penghapusan:**
```
┌──────────────────────────────┐
│   Hapus Modul?               │
│                              │
│   "Tata Cara Wudhu" beserta  │
│   seluruh langkahnya akan    │
│   dihapus permanen.          │
│                              │
│   [Batal]      [Hapus]       │
└──────────────────────────────┘
```
- Tombol "Hapus" berwarna merah
- Jika dikonfirmasi: hapus dokumen di Firestore + seluruh subcollection
  steps-nya, lalu refresh daftar

---

### LANGKAH 3 — `InputModulScreen.kt` (Form Input / Edit Modul)

**Deskripsi:** Halaman ini digunakan untuk membuat modul baru ATAU
mengedit modul yang sudah ada. UI menggunakan pendekatan floating sheet /
layer berlapis mirip seperti `ManajemenAkunScreen` yang sudah ada di project.

**Mode Tambah Baru:** parameter `moduleId = null`
**Mode Edit:** parameter `moduleId = "xxx"` → pre-fill semua field dari Firestore

**Struktur UI halaman (scroll vertikal penuh):**

```
[AppBar: "Tambah Modul Ajar" / "Edit Modul"]

━━━━━━━━━━━━━━━━━━━━━━━━━━
 INFORMASI MODUL
━━━━━━━━━━━━━━━━━━━━━━━━━━

 Judul Modul *
 [________________________________]
 contoh: Tata Cara Wudhu

 Kategori *
 [________________________________]
 contoh: Bersuci, Sholat, Doa

 Deskripsi Modul (opsional)
 [________________________________]
 [________________________________]

━━━━━━━━━━━━━━━━━━━━━━━━━━
 LANGKAH 1
━━━━━━━━━━━━━━━━━━━━━━━━━━

 Judul Langkah *
 [________________________________]
 contoh: Membasuh Kedua Tangan

 Deskripsi *
 [________________________________]  (multiline, minimal 3 baris)
 [________________________________]
 [________________________________]

 Bacaan Arab (opsional)
 [________________________________]

 Bacaan Latin (opsional)
 [________________________________]

 Arti (opsional)
 [________________________________]

 [ + Langkah/Deskripsi Lainnya ]   ← tombol outlined/text button

━━━━━━━━━━━━━━━━━━━━━━━━━━
 LANGKAH 2  (muncul setelah klik tombol di atas)
━━━━━━━━━━━━━━━━━━━━━━━━━━
 ... (field yang sama persis dengan Langkah 1)
 [ + Langkah/Deskripsi Lainnya ]
 [ 🗑 Hapus Langkah Ini ]          ← hanya muncul jika lebih dari 1 langkah

━━━━━━━━━━━━━━━━━━━━━━━━━━

 [        Upload Modul        ]    ← tombol primary, full width, di paling bawah
```

**Spesifikasi tombol "Langkah/Deskripsi Lainnya":**
- Setiap klik menambah satu blok form langkah baru di bawahnya
- Langkah baru otomatis bernomor urut (Langkah 2, Langkah 3, dst.)
- Tidak ada batas maksimum langkah
- Langkah pertama tidak punya tombol hapus; langkah ke-2 dst. punya
  tombol "Hapus Langkah Ini"

**Spesifikasi tombol "Upload Modul":**
- Aktif hanya jika: Judul Modul, Kategori, dan minimal 1 langkah
  (Judul Langkah + Deskripsi) sudah terisi
- Jika diklik → tampilkan **Dialog Konfirmasi Upload:**
  ```
  ┌──────────────────────────────┐
  │   Upload Modul?              │
  │                              │
  │   Modul akan langsung        │
  │   terlihat oleh siswa.       │
  │                              │
  │   [Batal]    [Upload]        │
  └──────────────────────────────┘
  ```
- Jika dikonfirmasi → simpan ke Firestore → tampilkan Snackbar
  "Modul berhasil diupload!" → kembali ke `TambahModulAjarScreen`

**Alur penyimpanan ke Firestore:**
1. Buat dokumen baru di `dynamic_modules/` dengan field:
   `title, description, category, createdByUid, createdByName,
   isPublished=true, createdAt, updatedAt`
2. Untuk setiap langkah, buat dokumen baru di subcollection
   `dynamic_modules/{moduleId}/steps/` dengan field:
   `stepOrder, title, description, arabicText, latinText, translationText`
3. Semua operasi dalam satu batch write agar atomik

**Alur edit (mode edit):**
1. Saat layar dibuka dengan `moduleId`, fetch data modul + semua steps
2. Pre-fill semua field
3. Saat "Upload Modul" diklik → update dokumen modul + hapus semua
   steps lama + tulis ulang steps baru + update `updatedAt`

---

### LANGKAH 4 — Tambah MenuUtamaCard di `HomeSiswaScreen.kt`

**Aturan:** Tambahkan `MenuUtamaCard` BARU di bawah card "Evaluasi Gerakan"
yang sudah ada. Jangan ubah card "Belajar Gerakan" dan "Evaluasi Gerakan".

**Tata letak yang diinginkan:**
```
[  Belajar Gerakan & Bacaan  ]   ← SUDAH ADA, TIDAK DIUBAH
[     Evaluasi Gerakan       ]   ← SUDAH ADA, TIDAK DIUBAH
[       Modul Lainnya        ]   ← CARD BARU di bawahnya
```

**Spesifikasi card baru:**
- Teks: **"Modul Lainnya"**
- Subtitle: "Materi tambahan dari guru"
- Ikon: ikon buku atau folder (sesuai gaya ikon yang sudah ada)
- Warna: sesuaikan dengan tema aplikasi
- Aksi klik: navigasi ke route `modul_lainnya_siswa`

---

### LANGKAH 5 — `ModulLainnyaScreen.kt` (Halaman Daftar Modul Siswa)

**Deskripsi:** Halaman yang diakses siswa untuk melihat semua modul
dinamis yang sudah dipublish (isPublished = true).

**Kondisi tampilan:**

**A. Jika belum ada modul:**
```
[AppBar: "Modul Lainnya"]

        📚
   Belum ada modul belajar
         terbaru
```

**B. Jika ada modul:**
```
[AppBar: "Modul Lainnya"]

┌─────────────────────────────┐
│ Tata Cara Wudhu             │
│ Bersuci • 5 langkah         │
│                    [Mulai →]│
└─────────────────────────────┘
┌─────────────────────────────┐
│ Tata Cara Tayamum           │
│ Bersuci • 3 langkah         │
│                    [Mulai →]│
└─────────────────────────────┘
```

**Spesifikasi:**
- `TopAppBar` dengan judul "Modul Lainnya" dan tombol back
- Query Firestore: `dynamic_modules` where `isPublished == true`
  diurutkan berdasarkan `createdAt` descending (terbaru di atas)
- Klik card → navigasi ke `DetailModulDinamisScreen` dengan `moduleId`

---

### LANGKAH 6 — `DetailModulDinamisScreen.kt` (Stepper Detail Modul)

**Deskripsi:** Halaman stepper untuk membaca modul dinamis langkah demi
langkah. UI dibuat semirip mungkin dengan `DetailGerakanStepperContent`
yang sudah ada di project, agar konsistensi tampilan terjaga.

**Struktur UI (mengikuti pola DetailGerakanStepperContent):**

```
[AppBar: judul modul]

Progress Bar  ━━━━━━━━━━░░░░  Langkah 1 dari 5

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Membasuh Kedua Tangan          ← title langkah (heading)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Basuh kedua tangan hingga      ← description (body text)
  pergelangan tangan sebanyak
  3 kali dimulai dari ujung
  jari hingga pergelangan...

  ┌──────────────────────────┐
  │  نَوَيْتُ الْوُضُوْءَ   │   ← arabicText (jika tidak kosong)
  └──────────────────────────┘

  Nawaitul wudhuu...             ← latinText (jika tidak kosong)

  "Aku niat berwudhu..."         ← translationText (jika tidak kosong)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[  Sebelumnya  ]        [  Selanjutnya  ]
                        (atau [Selesai] di langkah terakhir)
```

**Spesifikasi:**
- Fetch data: ambil dokumen modul + semua steps dari subcollection,
  urutkan berdasarkan `stepOrder` ascending
- Field `arabicText`, `latinText`, `translationText`: tampilkan
  section-nya HANYA jika nilainya tidak null dan tidak kosong ("")
- Progress bar dan keterangan "Langkah X dari Y" di bagian atas
- Tombol "Sebelumnya" disabled di langkah pertama
- Tombol "Selanjutnya" berubah menjadi "Selesai" di langkah terakhir
- Klik "Selesai" → kembali ke `ModulLainnyaScreen`

---

### LANGKAH 7 — `DynamicModule.kt` dan `ModuleStep.kt` (Data Class)

```kotlin
// FILE BARU: DynamicModule.kt
data class DynamicModule(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val createdByUid: String = "",
    val createdByName: String = "",
    val isPublished: Boolean = true,
    val createdAt: com.google.firebase.Timestamp? = null,
    val updatedAt: com.google.firebase.Timestamp? = null,
    val stepCount: Int = 0  // field helper, isi manual saat fetch
)

// FILE BARU: ModuleStep.kt
data class ModuleStep(
    val id: String = "",
    val moduleId: String = "",
    val stepOrder: Int = 1,
    val title: String = "",
    val description: String = "",
    val arabicText: String = "",
    val latinText: String = "",
    val translationText: String = ""
)
```

---

### LANGKAH 8 — `DynamicModuleRepository.kt`

```kotlin
// FILE BARU: DynamicModuleRepository.kt
// Semua fungsi suspend, dipanggil dari ViewModel dalam coroutine scope

class DynamicModuleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val modulesCollection = db.collection("dynamic_modules")

    // Ambil semua modul milik guru (untuk TambahModulAjarScreen)
    suspend fun getModulesByGuru(uid: String): List<DynamicModule>

    // Ambil semua modul published (untuk ModulLainnyaScreen siswa)
    suspend fun getPublishedModules(): List<DynamicModule>

    // Ambil semua langkah dari satu modul (untuk DetailModulDinamisScreen)
    suspend fun getStepsByModuleId(moduleId: String): List<ModuleStep>

    // Simpan modul baru beserta semua langkahnya (batch write)
    suspend fun saveModule(module: DynamicModule, steps: List<ModuleStep>): String

    // Update modul yang sudah ada (hapus steps lama, tulis ulang)
    suspend fun updateModule(module: DynamicModule, steps: List<ModuleStep>)

    // Hapus modul beserta semua langkahnya
    suspend fun deleteModule(moduleId: String)
}
```

---

### LANGKAH 9 — `ModulAjarViewModel.kt`

```kotlin
// FILE BARU: ModulAjarViewModel.kt
// Satu ViewModel dipakai bersama oleh screen guru dan siswa

class ModulAjarViewModel : ViewModel() {

    // State untuk TambahModulAjarScreen
    val guruModules: StateFlow<List<DynamicModule>>
    val isLoading: StateFlow<Boolean>
    val errorMessage: StateFlow<String?>

    // State untuk InputModulScreen
    val moduleTitle: MutableStateFlow<String>
    val moduleCategory: MutableStateFlow<String>
    val moduleDescription: MutableStateFlow<String>
    val steps: MutableStateFlow<List<ModuleStepInput>>

    // State untuk ModulLainnyaScreen (siswa)
    val publishedModules: StateFlow<List<DynamicModule>>

    // State untuk DetailModulDinamisScreen
    val currentModule: StateFlow<DynamicModule?>
    val currentSteps: StateFlow<List<ModuleStep>>
    val currentStepIndex: MutableStateFlow<Int>

    // Fungsi
    fun loadGuruModules(uid: String)
    fun loadPublishedModules()
    fun loadModuleDetail(moduleId: String)
    fun addStep()
    fun removeStep(index: Int)
    fun saveModule(createdByUid: String, createdByName: String)
    fun updateModule(moduleId: String)
    fun deleteModule(moduleId: String)
    fun nextStep()
    fun previousStep()
}

// Data class helper untuk state form input
data class ModuleStepInput(
    val title: String = "",
    val description: String = "",
    val arabicText: String = "",
    val latinText: String = "",
    val translationText: String = ""
)
```

---

### LANGKAH 10 — Navigasi (Tambah Route Baru di Nav Graph)

Tambahkan route berikut di file nav graph yang sudah ada.
**Jangan ubah route yang sudah ada, hanya tambahkan di bawahnya.**

```kotlin
// Route baru untuk guru
composable("tambah_modul_ajar") {
    TambahModulAjarScreen(navController = navController)
}
composable("input_modul?moduleId={moduleId}",
    arguments = listOf(navArgument("moduleId") {
        nullable = true; defaultValue = null
    })
) { backStackEntry ->
    InputModulScreen(
        navController = navController,
        moduleId = backStackEntry.arguments?.getString("moduleId")
    )
}

// Route baru untuk siswa
composable("modul_lainnya_siswa") {
    ModulLainnyaScreen(navController = navController)
}
composable("detail_modul_dinamis/{moduleId}") { backStackEntry ->
    DetailModulDinamisScreen(
        navController = navController,
        moduleId = backStackEntry.arguments?.getString("moduleId") ?: ""
    )
}
```

---

## Alur Lengkap Per Peran

### Alur Guru (Tambah Modul Baru)
```
HomeGuruScreen
    → klik card "Tambah Modul Ajar"
TambahModulAjarScreen (daftar kosong)
    → klik FAB "+"
InputModulScreen
    → isi Judul Modul, Kategori
    → isi Judul Langkah 1 + Deskripsi 1
    → klik "+ Langkah/Deskripsi Lainnya" (opsional, tambah langkah 2, 3, dst.)
    → klik "Upload Modul"
    → Dialog konfirmasi → klik "Upload"
    → data tersimpan ke Firestore
    → Snackbar "Modul berhasil diupload!"
TambahModulAjarScreen (modul baru muncul sebagai card)
```

### Alur Guru (Edit Modul)
```
TambahModulAjarScreen
    → klik ikon ✏️ pada card modul
InputModulScreen (mode edit, semua field ter-prefill)
    → ubah field yang ingin diubah
    → klik "Upload Modul"
    → Dialog konfirmasi → klik "Upload"
    → data diperbarui di Firestore
TambahModulAjarScreen (card modul menampilkan data terbaru)
```

### Alur Guru (Hapus Modul)
```
TambahModulAjarScreen
    → klik ikon 🗑️ pada card modul
    → Dialog konfirmasi hapus
    → klik "Hapus"
    → modul + semua steps dihapus dari Firestore
TambahModulAjarScreen (card modul hilang dari daftar)
```

### Alur Siswa (Baca Modul)
```
HomeSiswaScreen
    → klik card "Modul Lainnya"
ModulLainnyaScreen
    → jika belum ada modul → tampilkan pesan kosong
    → jika ada modul → tampilkan daftar card
    → klik salah satu card modul
DetailModulDinamisScreen (stepper langkah 1)
    → baca konten langkah 1
    → klik "Selanjutnya" → langkah 2
    → ... dst.
    → di langkah terakhir klik "Selesai"
ModulLainnyaScreen
```

---

## Checklist Implementasi

### Data Layer
- [ ] Buat `DynamicModule.kt`
- [ ] Buat `ModuleStep.kt`
- [ ] Buat `DynamicModuleRepository.kt` dengan semua fungsi CRUD
- [ ] Buat `ModulAjarViewModel.kt`

### UI Guru
- [ ] Tambah card "Tambah Modul Ajar" di `HomeGuruScreen.kt` (TAMBAH SAJA)
- [ ] Buat `TambahModulAjarScreen.kt` (daftar modul + FAB + dialog hapus)
- [ ] Buat `InputModulScreen.kt` (form input + tambah langkah + dialog upload)

### UI Siswa
- [ ] Tambah MenuUtamaCard "Modul Lainnya" di `HomeSiswaScreen.kt` (TAMBAH SAJA)
- [ ] Buat `ModulLainnyaScreen.kt` (daftar modul atau pesan kosong)
- [ ] Buat `DetailModulDinamisScreen.kt` (stepper mirip DetailGerakanStepperContent)

### Navigasi
- [ ] Tambah 4 route baru di nav graph (TAMBAH SAJA, jangan ubah route lama)

---

## Catatan Penting untuk AI

1. **Referensi UI yang harus ditiru:**
   - Form input guru → mirip `ManajemenAkunScreen` (floating/layer style)
   - Stepper detail modul → mirip `DetailGerakanStepperContent`
   - Card modul di halaman siswa → mirip `MenuUtamaCard` yang sudah ada

2. **Firebase UID guru** diambil dari
   `FirebaseAuth.getInstance().currentUser?.uid` — tidak perlu autentikasi baru.

3. **Semua field opsional** (`arabicText`, `latinText`, `translationText`)
   disimpan sebagai string kosong `""` jika tidak diisi guru, dan
   **tidak ditampilkan** di `DetailModulDinamisScreen` jika nilainya kosong.

4. **Tidak ada dependency baru** yang perlu ditambahkan —
   Firebase Firestore sudah terpasang dari fitur sebelumnya.

5. **Konsistensi visual:** gunakan warna, font, padding, dan komponen
   yang sama dengan yang sudah ada di project (tema hijau #388E3C,
   Material Design 3, style card yang sudah ada).

6. **Semua operasi Firestore** dijalankan dalam `viewModelScope.launch`
   dan dibungkus `try-catch` untuk error handling.

7. **Jangan buat ulang** komponen yang sudah ada (seperti `MenuUtamaCard`,
   `TopAppBar`, dll.) — gunakan komponen yang sudah ada di project.
