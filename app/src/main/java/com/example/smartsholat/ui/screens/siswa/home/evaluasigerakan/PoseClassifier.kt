package com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel

object PoseClassifier {

    // PERBAIKAN: Tambah class TIDAK_TERDETEKSI
    enum class PoseLabel {
        BERDIRI_ITIDAL, TAKBIR, SEDEKAP, RUKU, SUJUD, TASYAHUD, SALAM, TRANSISI, TIDAK_TERDETEKSI
    }

    fun PoseLabel.displayName(): String = when (this) {
        PoseLabel.BERDIRI_ITIDAL -> "Berdiri / I'tidal"
        PoseLabel.TAKBIR         -> "Takbir"
        PoseLabel.SEDEKAP        -> "Sedekap"
        PoseLabel.RUKU           -> "Ruku'"
        PoseLabel.SUJUD          -> "Sujud"
        PoseLabel.TASYAHUD       -> "Tasyahud / Duduk"
        PoseLabel.SALAM          -> "Salam"
        PoseLabel.TRANSISI       -> "..."
        PoseLabel.TIDAK_TERDETEKSI -> "Tidak terdeteksi"
    }

    private var interpreter: Interpreter? = null

    /** Confidence score dari hasil klasifikasi terakhir (0f–1f) */
    var lastConfidence: Float = 0f
        private set

    fun initModel(context: Context) {
        if (interpreter == null) {
            try {
                val afd = context.assets.openFd("model_sholat.tflite")
                val stream = FileInputStream(afd.fileDescriptor)
                val channel = stream.channel
                val buffer = channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)

                interpreter = Interpreter(buffer, Interpreter.Options().apply { numThreads = 2 })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun classify(rawKp: Array<Triple<Float, Float, Float>>, bbox: FloatArray): PoseLabel {
        if (interpreter == null) return PoseLabel.TRANSISI

        val cx = bbox[0]
        val cy = bbox[1]
        val w  = bbox[2]
        val h  = bbox[3]

        val x_min = cx - (w / 2f)
        val y_min = cy - (h / 2f)

        val inputFeature = FloatArray(34)
        var index = 0

        for (i in 0..16) {
            val x_mentah = rawKp[i].first
            val y_mentah = rawKp[i].second

            if (x_mentah == 0f && y_mentah == 0f) {
                inputFeature[index++] = 0f
                inputFeature[index++] = 0f
            } else {
                inputFeature[index++] = if (w > 0) (x_mentah - x_min) / w else 0f
                inputFeature[index++] = if (h > 0) (y_mentah - y_min) / h else 0f
            }
        }

        val inputArray = arrayOf(inputFeature)
        val outputArray = arrayOf(FloatArray(7))

        interpreter?.run(inputArray, outputArray)

        val probabilitas = outputArray[0]
        var maxIdx = -1
        var maxProb = -1f

        for (i in probabilitas.indices) {
            if (probabilitas[i] > maxProb) {
                maxProb = probabilitas[i]
                maxIdx = i
            }
        }

        lastConfidence = maxProb

        if (maxProb < 0.60f) return PoseLabel.TRANSISI

        return when (maxIdx) {
            0 -> PoseLabel.BERDIRI_ITIDAL
            1 -> PoseLabel.RUKU
            2 -> PoseLabel.SALAM
            3 -> PoseLabel.SEDEKAP
            4 -> PoseLabel.SUJUD
            5 -> PoseLabel.TAKBIR
            6 -> PoseLabel.TASYAHUD
            else -> PoseLabel.TRANSISI
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    fun reset() { }
}