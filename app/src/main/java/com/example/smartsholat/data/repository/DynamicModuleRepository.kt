package com.example.smartsholat.data.repository

import com.example.smartsholat.data.model.DynamicModule
import com.example.smartsholat.data.model.ModuleStep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class DynamicModuleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val modulesCollection = db.collection("dynamic_modules")

    /**
     * Ambil semua modul milik guru tertentu (untuk TambahModulAjarScreen).
     * Menghitung stepCount per modul secara manual dari subcollection.
     */
    suspend fun getModulesByGuru(uid: String): List<DynamicModule> {
        val snapshot = modulesCollection
            .whereEqualTo("createdByUid", uid)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val stepSnapshot = modulesCollection.document(doc.id)
                .collection("steps")
                .get()
                .await()

            DynamicModule(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                category = doc.getString("category") ?: "",
                createdByUid = doc.getString("createdByUid") ?: "",
                createdByName = doc.getString("createdByName") ?: "",
                isPublished = doc.getBoolean("isPublished") ?: true,
                createdAt = doc.getTimestamp("createdAt"),
                updatedAt = doc.getTimestamp("updatedAt"),
                stepCount = stepSnapshot.size()
            )
        }.sortedWith { a, b ->
            val t1 = a.createdAt
            val t2 = b.createdAt
            if (t1 == null && t2 == null) 0
            else if (t1 == null) 1
            else if (t2 == null) -1
            else t2.compareTo(t1)
        }
    }

    /**
     * Ambil semua modul yang dipublish (untuk ModulLainnyaScreen siswa).
     */
    suspend fun getPublishedModules(): List<DynamicModule> {
        val snapshot = modulesCollection
            .whereEqualTo("isPublished", true)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val stepSnapshot = modulesCollection.document(doc.id)
                .collection("steps")
                .get()
                .await()

            DynamicModule(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                category = doc.getString("category") ?: "",
                createdByUid = doc.getString("createdByUid") ?: "",
                createdByName = doc.getString("createdByName") ?: "",
                isPublished = doc.getBoolean("isPublished") ?: true,
                createdAt = doc.getTimestamp("createdAt"),
                updatedAt = doc.getTimestamp("updatedAt"),
                stepCount = stepSnapshot.size()
            )
        }.sortedWith { a, b ->
            val t1 = a.createdAt
            val t2 = b.createdAt
            if (t1 == null && t2 == null) 0
            else if (t1 == null) 1
            else if (t2 == null) -1
            else t2.compareTo(t1)
        }
    }

    /**
     * Ambil detail satu modul berdasarkan moduleId.
     */
    suspend fun getModuleById(moduleId: String): DynamicModule? {
        val doc = modulesCollection.document(moduleId).get().await()
        if (!doc.exists()) return null

        val stepSnapshot = modulesCollection.document(doc.id)
            .collection("steps")
            .get()
            .await()

        return DynamicModule(
            id = doc.id,
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: "",
            category = doc.getString("category") ?: "",
            createdByUid = doc.getString("createdByUid") ?: "",
            createdByName = doc.getString("createdByName") ?: "",
            isPublished = doc.getBoolean("isPublished") ?: true,
            createdAt = doc.getTimestamp("createdAt"),
            updatedAt = doc.getTimestamp("updatedAt"),
            stepCount = stepSnapshot.size()
        )
    }

    /**
     * Ambil semua langkah dari satu modul, diurutkan berdasarkan stepOrder.
     */
    suspend fun getStepsByModuleId(moduleId: String): List<ModuleStep> {
        val snapshot = modulesCollection.document(moduleId)
            .collection("steps")
            .orderBy("stepOrder", Query.Direction.ASCENDING)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            ModuleStep(
                id = doc.id,
                moduleId = moduleId,
                stepOrder = (doc.getLong("stepOrder") ?: 1L).toInt(),
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                arabicText = doc.getString("arabicText") ?: "",
                latinText = doc.getString("latinText") ?: "",
                translationText = doc.getString("translationText") ?: ""
            )
        }
    }

    /**
     * Simpan modul baru beserta semua langkahnya menggunakan batch write.
     * @return ID dokumen modul yang baru dibuat
     */
    suspend fun saveModule(module: DynamicModule, steps: List<ModuleStep>): String {
        val batch = db.batch()
        val now = Timestamp.now()

        // 1. Buat dokumen modul baru
        val moduleRef = modulesCollection.document()
        val moduleData = hashMapOf(
            "title" to module.title,
            "description" to module.description,
            "category" to module.category,
            "createdByUid" to module.createdByUid,
            "createdByName" to module.createdByName,
            "isPublished" to true,
            "createdAt" to now,
            "updatedAt" to now
        )
        batch.set(moduleRef, moduleData)

        // 2. Buat dokumen untuk setiap langkah
        steps.forEachIndexed { index, step ->
            val stepRef = moduleRef.collection("steps").document()
            val stepData = hashMapOf(
                "stepOrder" to (index + 1),
                "title" to step.title,
                "description" to step.description,
                "arabicText" to step.arabicText,
                "latinText" to step.latinText,
                "translationText" to step.translationText
            )
            batch.set(stepRef, stepData)
        }

        batch.commit().await()
        return moduleRef.id
    }

    /**
     * Update modul yang sudah ada: update field modul + hapus steps lama + tulis ulang steps baru.
     */
    suspend fun updateModule(module: DynamicModule, steps: List<ModuleStep>) {
        val batch = db.batch()
        val now = Timestamp.now()
        val moduleRef = modulesCollection.document(module.id)

        // 1. Update field modul
        val moduleData = hashMapOf(
            "title" to module.title,
            "description" to module.description,
            "category" to module.category,
            "isPublished" to true,
            "updatedAt" to now
        )
        batch.update(moduleRef, moduleData as Map<String, Any>)

        // 2. Hapus semua steps lama
        val oldSteps = moduleRef.collection("steps").get().await()
        for (doc in oldSteps.documents) {
            batch.delete(doc.reference)
        }

        // 3. Tulis ulang steps baru
        steps.forEachIndexed { index, step ->
            val stepRef = moduleRef.collection("steps").document()
            val stepData = hashMapOf(
                "stepOrder" to (index + 1),
                "title" to step.title,
                "description" to step.description,
                "arabicText" to step.arabicText,
                "latinText" to step.latinText,
                "translationText" to step.translationText
            )
            batch.set(stepRef, stepData)
        }

        batch.commit().await()
    }

    /**
     * Hapus modul beserta semua langkahnya dari Firestore.
     */
    suspend fun deleteModule(moduleId: String) {
        val moduleRef = modulesCollection.document(moduleId)

        // 1. Hapus semua steps di subcollection
        val stepsSnapshot = moduleRef.collection("steps").get().await()
        val batch = db.batch()
        for (doc in stepsSnapshot.documents) {
            batch.delete(doc.reference)
        }

        // 2. Hapus dokumen modul
        batch.delete(moduleRef)

        batch.commit().await()
    }
}
