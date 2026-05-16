package com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.smartsholat.R
import com.example.smartsholat.ui.screens.siswa.home.belajargerakan.DataBelajar
import com.example.smartsholat.ui.screens.siswa.home.evaluasigerakan.PoseClassifier.PoseLabel
import com.example.smartsholat.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

// ── Warna UI evaluasi ─────────────────────────────────────────
private val ColorSukses   = Color(0xFF4CAF50)
private val ColorAktif    = Color(0xFF2196F3)
private val ColorWarning  = Color(0xFFFFC107)
private val ColorOrange   = Color(0xFFFF9800) // Tambahan Warna Oranye
private val ColorDanger   = Color(0xFFF44336)
private val ColorOverlay  = Color.Black.copy(alpha = 0.55f)

// ── Data Class ────────────────────────────────────────────────
data class PanduanItem(
    val title: String,
    val desc: String,
    val imageRes: Int,
    val isWarning: Boolean = false
)

fun getDisplayName(label: PoseLabel): String {
    return when (label) {
        PoseLabel.BERDIRI_ITIDAL -> "Berdiri / I'tidal"
        PoseLabel.TAKBIR         -> "Takbir"
        PoseLabel.SEDEKAP        -> "Sedekap"
        PoseLabel.RUKU           -> "Ruku'"
        PoseLabel.SUJUD          -> "Sujud"
        PoseLabel.TASYAHUD       -> "Tasyahud / Duduk"
        PoseLabel.SALAM          -> "Salam"
        PoseLabel.TRANSISI       -> "Mencari postur..."
        PoseLabel.TIDAK_TERDETEKSI -> "Tidak ada tubuh di frame"
    }
}

@Composable
fun CameraEvaluasiContent(
    namaSholat: String,
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onEvaluasiSelesai: (String, Long, Int, String) -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sholat         = DataBelajar.daftarSholat.find { it.nama == namaSholat }
    val jumlahRakaat   = sholat?.jumlahRakaat ?: 2

    // ── STATE PANDUAN AWAL ──
    var showPanduan by remember { mutableStateOf(true) }
    var stepPanduan by remember { mutableIntStateOf(0) }
    val daftarPanduan = listOf(
        PanduanItem("Pencahayaan Cukup", "Pastikan Anda berada di ruangan yang terang agar AI dapat mendeteksi tubuh Anda dengan jelas.", R.drawable.ic_cahaya),
        PanduanItem("Posisi Kamera Diagonal", "Letakkan kamera secara diagonal agak menyamping agar seluruh bagian tubuh saat sholat terlihat utuh.", R.drawable.panduan_diagonal),
        PanduanItem("Saran Pakaian", "Pastikan tidak menggunakan baju yang sangat menutupi lekuk tubuh secara penuh agar AI dapat melacak tulang/postur Anda secara optimal.", R.drawable.ic_pakaian, true)
    )

    // ── STATE TTS ──
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale("id", "ID")
                isTtsReady = true
            }
        }
        tts.value = ttsInstance
        onDispose { ttsInstance.shutdown() }
    }

    // ── STATE NAVIGASI & TIMER ──
    val urutanGerakan  = remember { SholatConfig.buildUrutan(jumlahRakaat) }
    var currentRakaat      by remember { mutableIntStateOf(1) }
    var currentStepIndex   by remember { mutableIntStateOf(0) }

    var isUploading        by remember { mutableStateOf(false) }
    var hasSaved           by remember { mutableStateOf(false) }

    var startTime          by remember { mutableLongStateOf(0L) }
    var isCorrectPose      by remember { mutableStateOf(false) }
    var accumulatedTimeMs  by remember { mutableLongStateOf(0L) }
    var lastFrameTimeMs    by remember { mutableLongStateOf(0L) }
    var holdProgress       by remember { mutableStateOf(0f) }
    var showSuksesAnim     by remember { mutableStateOf(false) }
    var currentDetectedPose by remember { mutableStateOf(PoseLabel.TIDAK_TERDETEKSI) }

    // State Timer & Gagal -> DISET 60 DETIK (1 MENIT)
    var timeLeftMs by remember { mutableLongStateOf(60000L) }

    // State TTS feedback saat gerakan belum tepat (cooldown 10 detik)
    var lastIncorrectTtsTimeMs by remember { mutableLongStateOf(0L) }
    // Flag agar TTS "Bagus, Pertahankan Gerakan!" hanya diucapkan sekali per step
    var hasSaidCorrectTts by remember { mutableStateOf(false) }
    // Simpan sisa waktu saat gerakan pertama kali benar, agar bisa di-restore jika gagal hold
    var timeLeftWhenCorrectMs by remember { mutableLongStateOf(60000L) }
    val totalGagal = remember { mutableStateListOf<Pair<Int, String>>() }

    val stepsRakaat    = urutanGerakan[currentRakaat] ?: emptyList()
    val currentStep    = stepsRakaat.getOrNull(currentStepIndex)
    val totalStepAll   = urutanGerakan.values.sumOf { it.size }
    val stepGlobal     = run {
        var acc = 0
        for (r in 1 until currentRakaat) acc += (urutanGerakan[r]?.size ?: 0)
        acc + currentStepIndex
    }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { analysisExecutor.shutdown() } }

    // ── FUNGSI: SIMPAN KE FIRESTORE ──
    val simpanKeFirestore: (Long) -> Unit = { durasi ->
        if (!hasSaved && !isUploading) {
            hasSaved = true
            isUploading = true

            val db = FirebaseFirestore.getInstance()
            val userId = sessionManager.getUserId()

            if (userId != null) {
                val docRef = db.collection("riwayat_evaluasi").document()
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

                val mapGagal = totalGagal.groupBy { it.first } // Mengelompokkan berdasarkan rakaat
                var stringGagal = ""
                mapGagal.forEach { (rakaat, list) ->
                    stringGagal += "Rakaat ke-$rakaat:\n"
                    list.forEachIndexed { index, pair ->
                        stringGagal += "${index + 1}. ${pair.second}\n"
                    }
                }
                val gerakanGagalFormatted = stringGagal.trimEnd()

                val data = hashMapOf(
                    "durasiMillis" to durasi,
                    "id" to docRef.id,
                    "siswaId" to userId,
                    "namaSholat" to "Sholat $namaSholat",
                    "tanggal" to sdf.format(Date()),
                    "timestamp" to System.currentTimeMillis(),
                    "TotalGerakanGagal" to totalGagal.size,
                    "GerakanGagal" to gerakanGagalFormatted
                )

                docRef.set(data)
                    .addOnSuccessListener {
                        isUploading = false
                        onEvaluasiSelesai(namaSholat, durasi, totalGagal.size, gerakanGagalFormatted)
                    }
                    .addOnFailureListener {
                        hasSaved = false
                        isUploading = false
                        Log.e("Firestore", "Gagal menyimpan riwayat", it)
                    }
            } else {
                isUploading = false
            }
        }
    }

    // ── FUNGSI: ADVANCE STEP ──
    val advanceStep: (Boolean) -> Unit = { isFailed ->
        if (!hasSaved) {
            if (!isFailed) showSuksesAnim = true

            holdProgress = 0f
            accumulatedTimeMs = 0L
            isCorrectPose = false
            timeLeftMs = 60000L // Reset timer ke 60 detik (1 menit)
            lastIncorrectTtsTimeMs = 0L // Reset cooldown TTS
            hasSaidCorrectTts = false // Reset flag TTS gerakan benar

            val nextIndex = currentStepIndex + 1
            var nextStep: GerakanStep? = null

            if (nextIndex < stepsRakaat.size) {
                currentStepIndex = nextIndex
                nextStep = stepsRakaat[nextIndex]
            } else {
                val nextRakaat = currentRakaat + 1
                if (nextRakaat <= jumlahRakaat) {
                    currentRakaat = nextRakaat
                    currentStepIndex = 0
                    nextStep = urutanGerakan[nextRakaat]?.get(0)
                } else {
                    simpanKeFirestore(System.currentTimeMillis() - startTime)
                }
            }

            // Umpan balik suara untuk step berikutnya -> FEEDBACK DIPERLENGKAP
            if (nextStep != null) {
                if (isFailed) {
                    tts.value?.speak("Waktu habis. Lanjutkan ke ${nextStep.namaGerakan}. ${nextStep.feedbackText}", TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    val kataPujian = listOf("Bagus sekali!", "Hebat!", "Pintar!", "Luar biasa!").random()
                    tts.value?.speak(kataPujian, TextToSpeech.QUEUE_FLUSH, null, null)
                    tts.value?.speak(nextStep.feedbackText, TextToSpeech.QUEUE_ADD, null, null)
                }
            }
        }
    }

    // ── EFFECT: TTS AWAL ──
    LaunchedEffect(isTtsReady, showPanduan) {
        if (isTtsReady && !showPanduan && currentStepIndex == 0 && currentRakaat == 1) {
            tts.value?.speak(currentStep?.feedbackText ?: "", TextToSpeech.QUEUE_FLUSH, null, null)
            startTime = System.currentTimeMillis()
        }
    }

    // ── EFFECT: TIMER MUNDUR ──
    LaunchedEffect(isCorrectPose, showPanduan, currentStepIndex) {
        if (!showPanduan && !isUploading && !hasSaved && !isCorrectPose) {
            while (timeLeftMs > 0) {
                delay(50)
                timeLeftMs -= 50
                if (timeLeftMs <= 0) {
                    val stepName = currentStep?.namaGerakan ?: "Gerakan"
                    totalGagal.add(Pair(currentRakaat, stepName))
                    advanceStep(true)
                    break
                }
            }
        }
    }

    // ── EFFECT: ANIMASI SUKSES ──
    LaunchedEffect(showSuksesAnim) {
        if (showSuksesAnim) {
            delay(800)
            showSuksesAnim = false
        }
    }

    // ── FUNGSI: TANGKAP POSE DARI KAMERA ──
    val onPoseDetected: (PoseLabel) -> Unit = { label ->
        if (!showPanduan) {
            currentDetectedPose = label

            val now = System.currentTimeMillis()
            val deltaTime = if (lastFrameTimeMs == 0L) 0L else (now - lastFrameTimeMs)
            lastFrameTimeMs = now

            val targetLabel = currentStep?.targetLabel
            val benar = targetLabel != null && label == targetLabel

            if (benar) {
                // Simpan sisa waktu saat pertama kali pose benar (transisi salah -> benar)
                if (!isCorrectPose) {
                    timeLeftWhenCorrectMs = timeLeftMs
                }
                accumulatedTimeMs += deltaTime
                isCorrectPose = true

                // TTS: ucapkan "Bagus, Pertahankan Gerakan!" sekali saat pose tepat
                if (isTtsReady && !hasSaidCorrectTts) {
                    hasSaidCorrectTts = true
                    tts.value?.speak(
                        "Bagus, Pertahankan Gerakan!",
                        TextToSpeech.QUEUE_FLUSH, null, null
                    )
                }
            } else {
                if (isCorrectPose) {
                    timeLeftMs = timeLeftWhenCorrectMs // Kembalikan ke sisa waktu saat gerakan pertama kali benar
                    hasSaidCorrectTts = false // Reset agar bisa diucapkan lagi nanti
                }
                accumulatedTimeMs -= deltaTime
                if (accumulatedTimeMs < 0L) accumulatedTimeMs = 0L
                isCorrectPose = false

                // TTS: beri tahu siswa gerakan belum tepat + instruksi
                // Hanya setelah 10 detik berlalu (timeLeftMs <= 50000) agar tidak bentrok instruksi awal
                // Cooldown 10 detik antar pengucapan
                if (isTtsReady && timeLeftMs <= 50000L
                    && label != PoseLabel.TIDAK_TERDETEKSI && label != PoseLabel.TRANSISI
                ) {
                    val nowTts = System.currentTimeMillis()
                    if (nowTts - lastIncorrectTtsTimeMs > 10000L) {
                        lastIncorrectTtsTimeMs = nowTts
                        val instruksi = currentStep?.feedbackText ?: ""
                        tts.value?.speak(
                            "Gerakan belum tepat. $instruksi",
                            TextToSpeech.QUEUE_FLUSH, null, null
                        )
                    }
                }
            }

            val required = currentStep?.durasiDeteksiMs ?: 3000L
            holdProgress = (accumulatedTimeMs.toFloat() / required).coerceIn(0f, 1f)

            if (accumulatedTimeMs >= required && !isUploading) {
                advanceStep(false)
            }
        } else {
            lastFrameTimeMs = 0L
        }
    }

    // ── UI KAMERA & OVERLAY ──
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreviewWithPose(
            modifier        = Modifier.fillMaxSize(),
            lifecycleOwner  = lifecycleOwner,
            executor        = analysisExecutor,
            context         = context,
            onPoseDetected  = onPoseDetected
        )

        if (!showPanduan && !isUploading) {
            currentStep?.let { step ->
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = step.imageRes),
                        contentDescription = "Panduan Gerakan",
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .scale(pulseScale)
                            .padding(bottom = 80.dp),
                        contentScale = ContentScale.Fit,
                        alpha = 0.70f
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showPanduan,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.zIndex(10f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(
                            targetState = stepPanduan,
                            transitionSpec = {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            },
                            label = "PanduanSlideTransition"
                        ) { currentStepIndex ->
                            val currentPanduan = daftarPanduan[currentStepIndex]
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = currentPanduan.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (currentPanduan.isWarning) Color(0xFFD32F2F) else Color(0xFF388E3C), textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                                    Image(painter = painterResource(id = currentPanduan.imageRes), contentDescription = currentPanduan.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(text = currentPanduan.desc, fontSize = 14.sp, textAlign = TextAlign.Center, color = Color.DarkGray, lineHeight = 20.sp, modifier = Modifier.heightIn(min = 60.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                            daftarPanduan.forEachIndexed { index, _ ->
                                Box(modifier = Modifier.padding(horizontal = 4.dp).size(if (index == stepPanduan) 10.dp else 8.dp).clip(CircleShape).background(if (index <= stepPanduan) Color(0xFF388E3C) else Color.LightGray))
                            }
                        }
                        Button(
                            onClick = { if (stepPanduan < daftarPanduan.size - 1) stepPanduan++ else showPanduan = false },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = if (stepPanduan < daftarPanduan.size - 1) "LANJUT" else "SAYA MENGERTI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        if (isUploading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).zIndex(5f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (!showPanduan) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorOverlay)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Sholat $namaSholat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(text = "Rakaat $currentRakaat dari $jumlahRakaat", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                    Box(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress     = { (stepGlobal + 1).toFloat() / totalStepAll },
                    modifier     = Modifier.fillMaxWidth().height(6.dp).padding(horizontal = 16.dp).clip(RoundedCornerShape(3.dp)),
                    color        = ColorSukses,
                    trackColor   = Color.White.copy(alpha = 0.25f)
                )
                Text(text = "Tahap ${stepGlobal + 1} dari $totalStepAll", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 16.dp), textAlign = TextAlign.End)
            }

            currentStep?.let { step ->
                PanelGerakanMinimalis(
                    modifier            = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp, start = 24.dp, end = 24.dp),
                    step                = step,
                    currentDetectedPose = currentDetectedPose,
                    isCorrectPose       = isCorrectPose,
                    holdProgress        = holdProgress,
                    showSuksesAnim      = showSuksesAnim,
                    timeLeftMs          = timeLeftMs
                )
            }
        }
    }
}

@Composable
private fun PanelGerakanMinimalis(
    modifier: Modifier,
    step: GerakanStep,
    currentDetectedPose: PoseLabel,
    isCorrectPose: Boolean,
    holdProgress: Float,
    showSuksesAnim: Boolean,
    timeLeftMs: Long
) {
    val scale by animateFloatAsState(
        targetValue = if (showSuksesAnim) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            showSuksesAnim -> ColorSukses
            isCorrectPose -> ColorAktif
            else -> Color.White.copy(alpha = 0.3f)
        }, label = ""
    )

    val timerProgress by animateFloatAsState(
        targetValue = timeLeftMs / 60000f,
        animationSpec = tween(50, easing = LinearEasing),
        label = "TimerProgress"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing),
        label = "SmoothProgressBar"
    )

    val progressText = when {
        animatedProgress >= 0.95f -> "Bagus!"
        animatedProgress >= 0.60f -> "Sedikit lagi!"
        animatedProgress > 0.05f  -> "Pertahankan Posisi!"
        else -> "Siap-siap..."
    }

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(visible = !isCorrectPose && !showSuksesAnim) {
                val timerColor = when {
                    timeLeftMs > 30000L -> ColorSukses
                    timeLeftMs > 15000L -> ColorWarning
                    timeLeftMs > 7000L  -> ColorOrange
                    else                -> ColorDanger
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp)) {
                    CircularProgressIndicator(
                        progress = { timerProgress },
                        color = timerColor,
                        trackColor = Color.DarkGray,
                        strokeWidth = 4.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "${(timeLeftMs / 1000)}",
                        color = timerColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Text(
                text = if (showSuksesAnim) "✓ Berhasil!" else "Target Gerakan:",
                color = if (showSuksesAnim) ColorSukses else Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = step.namaGerakan,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(visible = !showSuksesAnim) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // PERBAIKAN: Jika tidak terdeteksi, warnanya Merah (ColorDanger) agar mencolok.
                        Text(
                            text = "Terdeteksi: ${getDisplayName(currentDetectedPose)}",
                            color = when {
                                isCorrectPose -> ColorSukses
                                currentDetectedPose == PoseLabel.TIDAK_TERDETEKSI -> ColorDanger
                                else -> ColorWarning
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth(0.8f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = ColorAktif,
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = progressText,
                        color = if (holdProgress > 0) ColorAktif else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (holdProgress > 0.6f) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreviewWithPose(
    modifier: Modifier,
    lifecycleOwner: LifecycleOwner,
    executor: java.util.concurrent.ExecutorService,
    context: android.content.Context,
    onPoseDetected: (PoseLabel) -> Unit
) {
    val currentOnPoseDetected by rememberUpdatedState(onPoseDetected)

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val analyzer = remember {
        YoloPoseAnalyzer(
            context = context,
            onPoseDetected = { pose -> currentOnPoseDetected(pose) }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
        var imageAnalysisRef: ImageAnalysis? = null

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor, analyzer)
                    imageAnalysisRef = it
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("CameraPreviewWithPose", "Binding kamera gagal", e)
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context))

        onDispose {
            imageAnalysisRef?.clearAnalyzer()
            analyzer.close()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}