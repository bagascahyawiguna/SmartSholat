package com.example.smartsholat.ui.screens.guru.akun

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartsholat.ui.screens.guru.DataSiswa
import com.example.smartsholat.ui.screens.guru.GuruViewModel

// --- 1. FUNGSI LOGIKA (STATEFUL) ---
@Composable
fun ManajemenAkunScreen() {
    val viewModel: GuruViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardData()
    }

    ManajemenAkunContent(
        isSiswaLoading = viewModel.isSiswaLoading,
        daftarSiswa = viewModel.daftarSiswa,
        onAddSiswa = { nama, nis, pin, kelas, onSuccess ->
            viewModel.tambahAkunSiswa(nama, nis, pin, kelas) { sukses ->
                if (sukses) Toast.makeText(context, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show()
                else Toast.makeText(context, "Gagal membuat akun", Toast.LENGTH_SHORT).show()
                onSuccess(sukses)
            }
        },
        onEditSiswa = { id, nama, nis, kelas, onSuccess ->
            viewModel.ubahAkunSiswa(id, nama, nis, kelas) { sukses ->
                if (sukses) Toast.makeText(context, "Akun diperbarui", Toast.LENGTH_SHORT).show()
                else Toast.makeText(context, "Gagal memperbarui akun", Toast.LENGTH_SHORT).show()
                onSuccess(sukses)
            }
        },
        onDeleteSiswa = { id, onSuccess ->
            viewModel.hapusAkunSiswa(id) { sukses ->
                if (sukses) Toast.makeText(context, "Siswa berhasil dihapus", Toast.LENGTH_SHORT).show()
                else Toast.makeText(context, "Gagal menghapus siswa", Toast.LENGTH_SHORT).show()
                onSuccess(sukses)
            }
        }
    )
}

// --- 2. FUNGSI TAMPILAN (STATELESS - UI ONLY) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenAkunContent(
    isSiswaLoading: Boolean,
    daftarSiswa: List<DataSiswa>,
    onAddSiswa: (String, String, String, String, (Boolean) -> Unit) -> Unit,
    onEditSiswa: (String, String, String, String, (Boolean) -> Unit) -> Unit,
    onDeleteSiswa: (String, (Boolean) -> Unit) -> Unit // Menerima callback onSuccess
) {
    // State untuk mengontrol visibilitas dialog
    var showAddDialog by remember { mutableStateOf(false) }
    var siswaToEdit by remember { mutableStateOf<DataSiswa?>(null) }
    var siswaToDelete by remember { mutableStateOf<DataSiswa?>(null) }

    // State pencarian
    var searchQuery by remember { mutableStateOf("") }

    // Filter daftar siswa berdasarkan query (nama, NIS, atau kelas)
    val filteredSiswa = remember(daftarSiswa, searchQuery) {
        if (searchQuery.isBlank()) daftarSiswa
        else daftarSiswa.filter { siswa ->
            siswa.nama.contains(searchQuery, ignoreCase = true) ||
            siswa.nis.contains(searchQuery, ignoreCase = true) ||
            siswa.kelas.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Akun", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Akun", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF388E3C),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // ── SEARCH BAR ──────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama, NIS, atau kelas...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = Color(0xFF388E3C)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Hapus pencarian",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF388E3C),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // ── KONTEN LIST ──────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isSiswaLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF388E3C))
                        }
                    }
                    daftarSiswa.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Belum ada data siswa.", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                    filteredSiswa.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tidak ada siswa yang cocok",
                                    color = Color.Gray,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = """"$searchQuery""",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp)
                        ) {
                            items(filteredSiswa) { siswa ->
                                AkunCard(
                                    siswa = siswa,
                                    onEditClick = { siswaToEdit = siswa },
                                    onDeleteClick = { siswaToDelete = siswa }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POP-UP DIALOGS ---

    // 1. Dialog Tambah
    if (showAddDialog) {
        FormAkunDialog(
            onDismiss = { showAddDialog = false },
            onSave = { nama, nis, pin, kelas ->
                onAddSiswa(nama, nis, pin, kelas) { sukses ->
                    if (sukses) showAddDialog = false
                }
            }
        )
    }

    // 2. Dialog Edit
    if (siswaToEdit != null) {
        EditAkunDialog(
            siswa = siswaToEdit!!,
            onDismiss = { siswaToEdit = null },
            onSave = { namaBaru, nisBaru, kelasBaru ->
                onEditSiswa(siswaToEdit!!.id, namaBaru, nisBaru, kelasBaru) { sukses ->
                    if (sukses) siswaToEdit = null
                }
            }
        )
    }

    // 3. Dialog Konfirmasi Hapus
    if (siswaToDelete != null) {
        AlertDialog(
            onDismissRequest = { siswaToDelete = null },
            title = { Text("Hapus Siswa") },
            text = { Text("Apakah Anda yakin ingin menghapus data ${siswaToDelete!!.nama}?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    onClick = {
                        onDeleteSiswa(siswaToDelete!!.id) { sukses ->
                            if (sukses) siswaToDelete = null
                        }
                    }
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { siswaToDelete = null }) { Text("Batal", color = Color.Gray) }
            }
        )
    }
}

// --- KOMPONEN KECIL ---
@Composable
fun AkunCard(siswa: DataSiswa, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = siswa.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "NIS: ${siswa.nis} | Kelas: ${siswa.kelas}", fontSize = 13.sp, color = Color.Gray)
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF388E3C))
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@Composable
fun FormAkunDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var nama by remember { mutableStateOf("") }
    var nis by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var kelas by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Akun Siswa", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = nis, onValueChange = { nis = it }, label = { Text("NIS") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("PIN") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = kelas, onValueChange = { kelas = it }, label = { Text("Kelas") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nama, nis, pin, kelas) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

@Composable
fun EditAkunDialog(siswa: DataSiswa, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    // State diinisialisasi dengan data siswa yang dipilih
    var nama by remember { mutableStateOf(siswa.nama) }
    var nis by remember { mutableStateOf(siswa.nis) }
    var kelas by remember { mutableStateOf(siswa.kelas) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Akun Siswa", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = nis, onValueChange = { nis = it }, label = { Text("NIS") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = kelas, onValueChange = { kelas = it }, label = { Text("Kelas") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nama, nis, kelas) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) { Text("Simpan Perubahan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

// --- 3. FUNGSI PREVIEW ---
@Preview(showBackground = true, device = Devices.PIXEL_4, showSystemUi = true)
@Composable
fun ManajemenAkunLengkapPreview() {
    val dummySiswa = listOf(
        DataSiswa(id = "1", nama = "Budi Santoso", nis = "2023001", kelas = "Kelas A (Tunawicara)"),
        DataSiswa(id = "2", nama = "Siti Aisyah", nis = "2023002", kelas = "Kelas B (Down Syndrome)")
    )

    Surface {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF388E3C), contentColor = Color.White) {
                    NavigationBarItem(icon = { Icon(Icons.Default.Home, "") }, label = { Text("Home") }, selected = false, onClick = {})
                    NavigationBarItem(icon = { Icon(Icons.Default.People, "") }, label = { Text("Siswa") }, selected = true, colors = NavigationBarItemDefaults.colors(indicatorColor = Color.White), onClick = {})
                    NavigationBarItem(icon = { Icon(Icons.Default.Person, "") }, label = { Text("Profil") }, selected = false, onClick = {})
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                ManajemenAkunContent(
                    isSiswaLoading = false,
                    daftarSiswa = dummySiswa,
                    onAddSiswa = { _, _, _, _, onSuccess -> onSuccess(true) },
                    onEditSiswa = { _, _, _, _, onSuccess -> onSuccess(true) },
                    onDeleteSiswa = { _, onSuccess -> onSuccess(true) }
                )
            }
        }
    }
}