package com.example.smartsholat.ui.screens.guru.modul

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartsholat.data.model.DynamicModule
import com.google.firebase.auth.FirebaseAuth

// ═══════════════════════════════════════════════════
// 1. FUNGSI LOGIKA (STATEFUL)
// ═══════════════════════════════════════════════════

@Composable
fun TambahModulAjarScreen(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onModulClick: (String) -> Unit
) {
    val viewModel: ModulAjarViewModel = viewModel()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val modules by viewModel.guruModules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(currentUid) {
        if (currentUid.isNotEmpty()) {
            viewModel.loadGuruModules(currentUid)
        }
    }

    TambahModulAjarContent(
        modules = modules,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onAddClick = onAddClick,
        onEditClick = onEditClick,
        onModulClick = onModulClick,
        onDeleteClick = { moduleId ->
            viewModel.deleteModule(moduleId) { /* handled by state update */ }
        }
    )
}

// ═══════════════════════════════════════════════════
// 2. FUNGSI TAMPILAN (STATELESS)
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahModulAjarContent(
    modules: List<DynamicModule>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onModulClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    // State untuk dialog konfirmasi hapus
    var modulToDelete by remember { mutableStateOf<DynamicModule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modul Ajar", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF388E3C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = Color(0xFF388E3C),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Modul", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF388E3C))
                    }
                }
                modules.isEmpty() -> {
                    // Tampilan kosong
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada modul yang\nditambahkan",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 16.dp, bottom = 88.dp // extra bottom for FAB
                        )
                    ) {
                        items(modules) { modul ->
                            ModulGuruCard(
                                modul = modul,
                                onClick = { onModulClick(modul.id) },
                                onEditClick = { onEditClick(modul.id) },
                                onDeleteClick = { modulToDelete = modul }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Penghapusan
    if (modulToDelete != null) {
        AlertDialog(
            onDismissRequest = { modulToDelete = null },
            title = { Text("Hapus Modul?", fontWeight = FontWeight.Bold) },
            text = {
                Text("\"${modulToDelete!!.title}\" beserta seluruh langkahnya akan dihapus permanen.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick(modulToDelete!!.id)
                        modulToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { modulToDelete = null }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════
// KOMPONEN: Card Modul untuk Guru
// ═══════════════════════════════════════════════════

@Composable
fun ModulGuruCard(
    modul: DynamicModule,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modul.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${modul.category} • ${modul.stepCount} langkah",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF388E3C)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = androidx.compose.ui.tooling.preview.Devices.PIXEL_4)
@Composable
private fun TambahModulAjarContentPreview() {
    TambahModulAjarContent(
        modules = listOf(
            DynamicModule(id = "1", title = "Tata Cara Wudhu", category = "Bersuci", stepCount = 6),
            DynamicModule(id = "2", title = "Doa Sehari-hari", category = "Doa", stepCount = 10)
        ),
        isLoading = false,
        onBackClick = {},
        onAddClick = {},
        onEditClick = {},
        onModulClick = {},
        onDeleteClick = {}
    )
}
