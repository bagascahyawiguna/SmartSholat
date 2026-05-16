package com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan.PoseClassifier.PoseLabel
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class YoloPoseAnalyzer(
    private val context: Context,
    private val onPoseDetected: (PoseLabel) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        private const val MODEL_FILENAME    = "yolov8n-pose-float32.tflite"
        private const val INPUT_SIZE        = 640
        private const val NUM_KEYPOINTS     = 17
        private const val OUTPUT_ROWS       = 56
        private const val OUTPUT_ANCHORS    = 8400
        private const val OBJ_CONF_THRESHOLD = 0.5f
    }

    private val interpreter: Interpreter
    private val inputBuffer: ByteBuffer
    private val outputBuffer: Array<Array<FloatArray>>

    private val lock = Any()
    @Volatile private var isClosed = false

    init {
        PoseClassifier.initModel(context)
        interpreter = Interpreter(
            loadModelFile(),
            Interpreter.Options().apply { numThreads = 4 }
        )
        inputBuffer = ByteBuffer
            .allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
            .apply { order(ByteOrder.nativeOrder()) }

        outputBuffer = Array(1) { Array(OUTPUT_ROWS) { FloatArray(OUTPUT_ANCHORS) } }
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (isClosed) {
            imageProxy.close()
            return
        }
        synchronized(lock) {
            if (isClosed) {
                imageProxy.close()
                return
            }
            try {
                val bitmap  = imageProxy.toBitmap()
                val rotated = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
                val scaled  = Bitmap.createScaledBitmap(rotated, INPUT_SIZE, INPUT_SIZE, true)
                bitmapToBuffer(scaled, inputBuffer)
                interpreter.run(inputBuffer, outputBuffer)
                val parseResult = parseOutputKeypoints(outputBuffer)
                if (parseResult != null) {
                    val (bbox, rawKp) = parseResult
                    val label = PoseClassifier.classify(rawKp, bbox)
                    onPoseDetected(label)
                } else {
                    onPoseDetected(PoseLabel.TIDAK_TERDETEKSI)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                imageProxy.close()
            }
        }
    }

    fun close() {
        isClosed = true
        synchronized(lock) {
            interpreter.close()
            PoseClassifier.close()
        }
    }

    private fun bitmapToBuffer(bitmap: Bitmap, buffer: ByteBuffer) {
        buffer.rewind()
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
            buffer.putFloat(((pixel shr  8) and 0xFF) / 255f)
            buffer.putFloat(( pixel         and 0xFF) / 255f)
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun parseOutputKeypoints(
        output: Array<Array<FloatArray>>
    ): Pair<FloatArray, Array<Triple<Float, Float, Float>>>? {
        val data = output[0]
        var bestConf   = OBJ_CONF_THRESHOLD
        var bestAnchor = -1
        for (i in 0 until OUTPUT_ANCHORS) {
            val conf = data[4][i]
            if (conf > bestConf) {
                bestConf   = conf
                bestAnchor = i
            }
        }

        if (bestAnchor == -1) return null

        val cx = data[0][bestAnchor]
        val cy = data[1][bestAnchor]
        val w  = data[2][bestAnchor]
        val h  = data[3][bestAnchor]
        val bbox = floatArrayOf(cx, cy, w, h)

        val kpts = Array(NUM_KEYPOINTS) { k ->
            val base = 5 + k * 3
            Triple(
                data[base    ][bestAnchor],
                data[base + 1][bestAnchor],
                data[base + 2][bestAnchor]
            )
        }
        return Pair(bbox, kpts)
    }

    private fun loadModelFile(): ByteBuffer {
        val afd     = context.assets.openFd(MODEL_FILENAME)
        val stream  = FileInputStream(afd.fileDescriptor)
        val channel = stream.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
    }
}