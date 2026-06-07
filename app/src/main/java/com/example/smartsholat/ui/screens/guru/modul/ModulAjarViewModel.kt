package com.example.smartsholat.ui.screens.guru.modul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartsholat.data.model.DynamicModule
import com.example.smartsholat.data.model.ModuleStep
import com.example.smartsholat.data.repository.DynamicModuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class helper untuk state form input langkah modul.
 */
data class ModuleStepInput(
    val title: String = "",
    val description: String = "",
    val arabicText: String = "",
    val latinText: String = "",
    val translationText: String = ""
)

/**
 * Satu ViewModel dipakai bersama oleh screen guru dan siswa.
 * Mengelola state untuk CRUD modul dan navigasi stepper.
 */
class ModulAjarViewModel : ViewModel() {

    private val repository = DynamicModuleRepository()

    // ─── State untuk TambahModulAjarScreen (daftar modul guru) ───
    private val _guruModules = MutableStateFlow<List<DynamicModule>>(emptyList())
    val guruModules: StateFlow<List<DynamicModule>> = _guruModules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ─── State untuk InputModulScreen (form input) ───
    val moduleTitle = MutableStateFlow("")
    val moduleCategory = MutableStateFlow("")
    val moduleDescription = MutableStateFlow("")
    val steps = MutableStateFlow(listOf(ModuleStepInput()))

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // ─── State untuk ModulLainnyaScreen (siswa) ───
    private val _publishedModules = MutableStateFlow<List<DynamicModule>>(emptyList())
    val publishedModules: StateFlow<List<DynamicModule>> = _publishedModules.asStateFlow()

    private val _isPublishedLoading = MutableStateFlow(false)
    val isPublishedLoading: StateFlow<Boolean> = _isPublishedLoading.asStateFlow()

    // ─── State untuk DetailModulDinamisScreen (stepper) ───
    private val _currentModule = MutableStateFlow<DynamicModule?>(null)
    val currentModule: StateFlow<DynamicModule?> = _currentModule.asStateFlow()

    private val _currentSteps = MutableStateFlow<List<ModuleStep>>(emptyList())
    val currentSteps: StateFlow<List<ModuleStep>> = _currentSteps.asStateFlow()

    val currentStepIndex = MutableStateFlow(0)

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    // ═══════════════════════════════════════════════════
    // FUNGSI UNTUK GURU — Daftar Modul
    // ═══════════════════════════════════════════════════

    fun loadGuruModules(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _guruModules.value = repository.getModulesByGuru(uid)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memuat daftar modul"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteModule(moduleId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteModule(moduleId)
                // Hapus dari list lokal agar UI langsung terupdate
                _guruModules.value = _guruModules.value.filter { it.id != moduleId }
                onResult(true)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal menghapus modul"
                onResult(false)
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // FUNGSI UNTUK GURU — Form Input/Edit Modul
    // ═══════════════════════════════════════════════════

    fun resetForm() {
        moduleTitle.value = ""
        moduleCategory.value = ""
        moduleDescription.value = ""
        steps.value = listOf(ModuleStepInput())
        _saveSuccess.value = false
    }

    fun loadModuleForEdit(moduleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val module = repository.getModuleById(moduleId)
                val moduleSteps = repository.getStepsByModuleId(moduleId)

                if (module != null) {
                    moduleTitle.value = module.title
                    moduleCategory.value = module.category
                    moduleDescription.value = module.description
                    steps.value = moduleSteps.map { step ->
                        ModuleStepInput(
                            title = step.title,
                            description = step.description,
                            arabicText = step.arabicText,
                            latinText = step.latinText,
                            translationText = step.translationText
                        )
                    }.ifEmpty { listOf(ModuleStepInput()) }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memuat data modul"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStepField(index: Int, updatedStep: ModuleStepInput) {
        val currentList = steps.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updatedStep
            steps.value = currentList
        }
    }

    fun addStep() {
        steps.value = steps.value + ModuleStepInput()
    }

    fun removeStep(index: Int) {
        if (steps.value.size > 1) {
            steps.value = steps.value.toMutableList().apply { removeAt(index) }
        }
    }

    fun saveModule(createdByUid: String, createdByName: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveSuccess.value = false
            try {
                val module = DynamicModule(
                    title = moduleTitle.value.trim(),
                    description = moduleDescription.value.trim(),
                    category = moduleCategory.value.trim(),
                    createdByUid = createdByUid,
                    createdByName = createdByName
                )
                val moduleSteps = steps.value.mapIndexed { index, input ->
                    ModuleStep(
                        stepOrder = index + 1,
                        title = input.title.trim(),
                        description = input.description.trim(),
                        arabicText = input.arabicText.trim(),
                        latinText = input.latinText.trim(),
                        translationText = input.translationText.trim()
                    )
                }
                repository.saveModule(module, moduleSteps)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal menyimpan modul"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateModule(moduleId: String) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveSuccess.value = false
            try {
                val module = DynamicModule(
                    id = moduleId,
                    title = moduleTitle.value.trim(),
                    description = moduleDescription.value.trim(),
                    category = moduleCategory.value.trim()
                )
                val moduleSteps = steps.value.mapIndexed { index, input ->
                    ModuleStep(
                        stepOrder = index + 1,
                        title = input.title.trim(),
                        description = input.description.trim(),
                        arabicText = input.arabicText.trim(),
                        latinText = input.latinText.trim(),
                        translationText = input.translationText.trim()
                    )
                }
                repository.updateModule(module, moduleSteps)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memperbarui modul"
            } finally {
                _isSaving.value = false
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // FUNGSI UNTUK SISWA — Daftar Modul Published
    // ═══════════════════════════════════════════════════

    fun loadPublishedModules() {
        viewModelScope.launch {
            _isPublishedLoading.value = true
            try {
                _publishedModules.value = repository.getPublishedModules()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memuat modul"
            } finally {
                _isPublishedLoading.value = false
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // FUNGSI UNTUK DETAIL MODUL (Stepper)
    // ═══════════════════════════════════════════════════

    fun loadModuleDetail(moduleId: String) {
        viewModelScope.launch {
            _isDetailLoading.value = true
            currentStepIndex.value = 0
            try {
                _currentModule.value = repository.getModuleById(moduleId)
                _currentSteps.value = repository.getStepsByModuleId(moduleId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal memuat detail modul"
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun nextStep() {
        if (currentStepIndex.value < _currentSteps.value.size - 1) {
            currentStepIndex.value++
        }
    }

    fun previousStep() {
        if (currentStepIndex.value > 0) {
            currentStepIndex.value--
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
