package com.example.smartsholat.ui.screens.guru.modul

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// ═══════════════════════════════════════════════════
// 1. FUNGSI LOGIKA (STATEFUL)
// ═══════════════════════════════════════════════════

@Composable
fun InputModulScreen(
    moduleId: String? = null,
    onBackClick: () -> Unit
) {
    val viewModel: ModulAjarViewModel = viewModel()
    val context = LocalContext.current
    val isEditMode = moduleId != null

    val title by viewModel.moduleTitle.collectAsState()
    val category by viewModel.moduleCategory.collectAsState()
    val description by viewModel.moduleDescription.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // Ambil nama guru dari Firestore
    var createdByName by remember { mutableStateOf("") }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(Unit) {
        viewModel.resetForm()
        // Fetch nama guru
        if (currentUid.isNotEmpty()) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUid)
                    .get()
                    .await()
                createdByName = doc.getString("nama") ?: "Guru"
            } catch (_: Exception) {
                createdByName = "Guru"
            }
        }
        // Jika mode edit, load data modul
        if (isEditMode) {
            viewModel.loadModuleForEdit(moduleId!!)
        }
    }

    // Tangkap event save berhasil
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            Toast.makeText(context, "Modul berhasil diupload!", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
    }

    InputModulContent(
        isEditMode = isEditMode,
        title = title,
        category = category,
        description = description,
        steps = steps,
        isSaving = isSaving,
        onTitleChange = { viewModel.moduleTitle.value = it },
        onCategoryChange = { viewModel.moduleCategory.value = it },
        onDescriptionChange = { viewModel.moduleDescription.value = it },
        onStepChange = { index, step -> viewModel.updateStepField(index, step) },
        onAddStep = { viewModel.addStep() },
        onRemoveStep = { viewModel.removeStep(it) },
        onBackClick = onBackClick,
        onUpload = {
            if (isEditMode) {
                viewModel.updateModule(moduleId!!)
            } else {
                viewModel.saveModule(currentUid, createdByName)
            }
        }
    )
}

// ═══════════════════════════════════════════════════
// 2. FUNGSI TAMPILAN (STATELESS)
// ═══════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputModulContent(
    isEditMode: Boolean,
    title: String,
    category: String,
    description: String,
    steps: List<ModuleStepInput>,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStepChange: (Int, ModuleStepInput) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (Int) -> Unit,
    onBackClick: () -> Unit,
    onUpload: () -> Unit
) {
    var showUploadDialog by remember { mutableStateOf(false) }

    // Validasi: judul, kategori, dan minimal 1 langkah (judul + deskripsi) wajib terisi
    val isFormValid = title.isNotBlank() &&
            category.isNotBlank() &&
            steps.any { it.title.isNotBlank() && it.description.isNotBlank() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Modul" else "Tambah Modul Ajar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION: INFORMASI MODUL
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "INFORMASI MODUL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF388E3C)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("Judul Modul *") },
                        placeholder = { Text("contoh: Tata Cara Wudhu") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF388E3C),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = onCategoryChange,
                        label = { Text("Kategori *") },
                        placeholder = { Text("contoh: Bersuci, Sholat, Doa") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF388E3C),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Deskripsi Modul (opsional)") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF388E3C),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // SECTION: LANGKAH-LANGKAH
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            steps.forEachIndexed { index, step ->
                StepInputCard(
                    stepNumber = index + 1,
                    step = step,
                    canDelete = steps.size > 1,
                    onStepChange = { updatedStep -> onStepChange(index, updatedStep) },
                    onRemoveClick = { onRemoveStep(index) },
                    onAddStepBelow = onAddStep
                )
            }

            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            // TOMBOL UPLOAD MODUL
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━
            Button(
                onClick = { showUploadDialog = true },
                enabled = isFormValid && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF388E3C),
                    disabledContainerColor = Color(0xFFBDBDBD)
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Upload Modul", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Spacer bawah agar tidak mepet
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialog Konfirmasi Upload
    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload Modul?", fontWeight = FontWeight.Bold) },
            text = { Text("Modul akan langsung terlihat oleh siswa.") },
            confirmButton = {
                Button(
                    onClick = {
                        showUploadDialog = false
                        onUpload()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) { Text("Upload") }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════
// KOMPONEN: Card Input Langkah
// ═══════════════════════════════════════════════════

@Composable
fun StepInputCard(
    stepNumber: Int,
    step: ModuleStepInput,
    canDelete: Boolean,
    onStepChange: (ModuleStepInput) -> Unit,
    onRemoveClick: () -> Unit,
    onAddStepBelow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "LANGKAH $stepNumber",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF388E3C)
            )

            OutlinedTextField(
                value = step.title,
                onValueChange = { onStepChange(step.copy(title = it)) },
                label = { Text("Judul Langkah *") },
                placeholder = { Text("contoh: Membasuh Kedua Tangan") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF388E3C),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = step.description,
                onValueChange = { onStepChange(step.copy(description = it)) },
                label = { Text("Deskripsi *") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF388E3C),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = step.arabicText,
                onValueChange = { onStepChange(step.copy(arabicText = it)) },
                label = { Text("Bacaan Arab (opsional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF388E3C),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = step.latinText,
                onValueChange = { onStepChange(step.copy(latinText = it)) },
                label = { Text("Bacaan Latin (opsional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF388E3C),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = step.translationText,
                onValueChange = { onStepChange(step.copy(translationText = it)) },
                label = { Text("Arti (opsional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF388E3C),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Tombol tambah langkah baru di bawah
            OutlinedButton(
                onClick = onAddStepBelow,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF388E3C))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Langkah/Deskripsi Lainnya", fontWeight = FontWeight.Bold)
            }

            // Tombol hapus langkah (hanya jika lebih dari 1 langkah)
            if (canDelete) {
                TextButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD32F2F))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus Langkah Ini")
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = androidx.compose.ui.tooling.preview.Devices.PIXEL_4)
@Composable
private fun InputModulContentPreview() {
    InputModulContent(
        isEditMode = false,
        title = "Tata Cara Wudhu",
        category = "Bersuci",
        description = "Panduan lengkap tata cara wudhu yang benar",
        steps = listOf(
            ModuleStepInput(
                title = "Membasuh Kedua Tangan",
                description = "Basuh kedua tangan hingga pergelangan sebanyak tiga kali.",
                arabicText = "بِسْمِ اللَّهِ",
                latinText = "Bismillah",
                translationText = "Dengan menyebut nama Allah"
            )
        ),
        isSaving = false,
        onTitleChange = {},
        onCategoryChange = {},
        onDescriptionChange = {},
        onStepChange = { _, _ -> },
        onAddStep = {},
        onRemoveStep = {},
        onBackClick = {},
        onUpload = {}
    )
}
