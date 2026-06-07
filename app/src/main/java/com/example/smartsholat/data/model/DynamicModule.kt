package com.example.smartsholat.data.model

import com.google.firebase.Timestamp

data class DynamicModule(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val createdByUid: String = "",
    val createdByName: String = "",
    val isPublished: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val stepCount: Int = 0  // field helper, diisi manual saat fetch
)
