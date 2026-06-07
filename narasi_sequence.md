# Narasi Sequence Diagram Aplikasi Smart Sholat

**1. Narasi UC-01: Login User**
Gambar 1 di atas memodelkan proses autentikasi pengguna, di mana aktor menginputkan kredensialnya melalui `LoginScreen` yang kemudian mengeksekusi metode `login()` pada entitas `User`. Setelah divalidasi, sistem menyimpan informasi pengguna (seperti ID, nama, dan peran) secara lokal melalui pemanggilan `saveSession()` pada `SessionManager`, yang selanjutnya mengembalikan status autentikasi sukses ke antarmuka untuk memicu navigasi otomatis menuju halaman dasbor utama.

**2. Narasi UC-02: Kelola Akun Siswa**
Gambar 2 di atas menjabarkan proses manajemen data pengguna yang dilakukan oleh Guru. Ketika membuka `ManajemenAkunScreen`, layar akan memuat daftar akun dari entitas tabel `Siswa`. Melalui layar ini, Guru dapat melakukan tiga operasi utama: menambahkan akun baru dengan mengisi form dan memanggil `tambahAkunSiswa()`, memperbarui data yang sudah ada via `editAkunSiswa()`, atau menghapus akun secara permanen melalui metode `hapusAkunSiswa()`. Setiap operasi yang berhasil akan mengembalikan nilai *boolean* sukses dan memicu pemunculan notifikasi (*toast*) di layar Guru.

**3. Narasi UC-03: Kelola Materi (Modul Ajar)**
Gambar 3 di atas memetakan alur pembuatan dan modifikasi modul dinamis oleh Guru. Guru mengakses `TambahModulAjarScreen` yang terlebih dahulu menarik daftar modul aktif dari entitas `DynamicModule`. Untuk menambah atau mengubah materi, sistem mengarahkan Guru ke `InputModulScreen` di mana formulir diisi dan dikirim menggunakan pemanggilan `tambahModul()` atau `editModul()`. Sebaliknya, untuk operasi penghapusan, Guru dapat langsung mengeksekusi `hapusModul()` dari daftar modul awal. Seluruh transaksi ini berujung pada pesan keberhasilan yang dirender ke layar antarmuka.

**4. Narasi UC-04: Monitoring Riwayat Evaluasi Siswa**
Gambar 4 di atas menggambarkan rute navigasi berlapis bagi Guru untuk memantau perkembangan evaluasi sholat para siswanya. Dimulai dari `HomeGuruScreen`, Guru masuk ke `DaftarSiswaScreen` yang secara dinamis mengambil data daftar nama dari entitas `Siswa`. Saat Guru memilih salah satu siswa, sistem menavigasi ke `RiwayatSiswaScreen` dan menjalankan `fetchRiwayatSiswa()` ke entitas `Riwayat_Evaluasi` untuk menarik histori nilai. Terakhir, Guru dapat mengklik salah satu histori untuk membuka `DetailRiwayatGuruScreen` yang menampilkan metrik kesalahan dan skor secara terperinci.

**5. Narasi UC-05: Profil User**
Gambar 5 di atas memetakan aliran penarikan data biodata kredensial untuk ditampilkan pada halaman profil. Sesaat setelah pengguna mengklik tab profil, `ProfileScreen` secara berurutan memanggil serangkaian metode pembacaan sesi ke objek tunggal `SessionManager`, mulai dari mengecek status login (`isLoggedIn()`), hingga menarik data identitas spesifik seperti `getNama()`, `getRole()`, dan `getNis()`. Begitu seluruh nilai data tersebut dikembalikan, layar merakitnya untuk dirender sebagai informasi profil pengguna secara visual.

**6. Narasi UC-06: Evaluasi Gerakan Sholat**
Gambar 6 di atas menggambarkan alur paling kompleks dari sistem, yaitu evaluasi gerakan secara *real-time* berbasis kecerdasan buatan. Setelah Siswa memilih sholat yang akan diuji dari `PilihSholatListContent`, layar `CameraEvaluasiContent` terbuka dan segera membangun urutan gerakan target melalui `SholatConfig`; layar ini lalu melakukan perulangan pemindaian gambar menggunakan `YoloPoseAnalyzer` yang mendelegasikan pengenalan posturnya ke `PoseClassifier`. Saat evaluasi tuntas, sistem menembakkan data hasil ke entitas `Riwayat_Evaluasi` untuk disimpan secara permanen, dan ditutup dengan pemindahan layar menuju `ResultEvaluasiScreen` untuk menampilkan perolehan skor akhir kepada Siswa.

**7. Narasi UC-07: Akses Materi (Belajar)**
Gambar 7 di atas menjabarkan dua percabangan rute pembelajaran, yaitu materi statis dan dinamis. Pada rute materi statis, Siswa bernavigasi dari `HomeSiswaScreen` ke `PilihSholatListContent` (yang mengambil daftar sholat dari `DataBelajar`), lalu beralih ke `DetailGerakanStepperContent` untuk merender panduan setiap langkah gerakan sholat. Sementara pada rute dinamis, Siswa menavigasi ke `ModulLainnyaScreen` untuk menarik kumpulan daftar modul guru dari entitas `DynamicModule`, lalu mengklik salah satu modul untuk membaca isinya secara komprehensif pada `DetailModulDinamisScreen`.

**8. Narasi UC-08: Lihat Riwayat Evaluasi (Siswa)**
Gambar 8 di atas memperlihatkan alur peninjauan histori nilai evaluasi dari sudut pandang siswa. Ketika pengguna membuka `RiwayatSiswaScreen`, layar tersebut segera memanggil metode `fetchRiwayatSiswa()` ke dalam entitas `Riwayat_Evaluasi` untuk memuat sekumpulan daftar riwayat sebelumnya. Saat pengguna mengklik salah satu kartu dari daftar yang dirender tersebut, sistem langsung menavigasikan layar menuju `DetailRiwayatScreen` guna memaparkan rincian evaluasi secara mendalam.

**9. Narasi UC-09: Logout User**
Gambar 9 di atas memodelkan proses terminasi sesi pengguna. Alur diawali ketika pengguna menekan tombol *Logout* dari dalam antarmuka `ProfileScreen`. Layar ini kemudian menginstruksikan `SessionManager` untuk menghapus seluruh jejak data kredensial lokal lewat metode `clearSession()`. Setelah menerima sinyal bahwa sesi telah bersih, sistem langsung mengeksekusi navigasi pemindahan layar secara paksa menuju `LoginScreen`, mengunci pengguna dari sistem hingga mereka melakukan autentikasi ulang.
