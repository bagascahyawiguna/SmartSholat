package com.example.smartsholat.data.model

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
