package com.safedriveai.app

import kotlin.math.sqrt
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import android.media.MediaPlayer
import android.content.Intent
import android.opengl.Matrix
import kotlin.math.atan2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker

class MonitoringActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var faceLandmarker: FaceLandmarker

    private val LEFT_EYE = intArrayOf(33, 160, 158, 133, 153, 144)

    private val RIGHT_EYE = intArrayOf(362, 385, 387, 263, 373, 380)

    private val MOUTH = intArrayOf(61, 291, 13, 14, 78, 308)

    private var eyeClosedFrames = 0
    private var wasDrowsy = false
    private var totalDrowsyEvents = 0

    private val DROWSY_FRAMES = 12
    private var eyeThreshold = 0.20f


    private var yawnFrames = 0
    private var wasYawning = false
    private var totalYawns = 0

    private val YAWN_FRAMES = 12
    private var yawnThreshold = 0.40f

    private var alarmVolume = 100


    //private var normalHeadPosition: Float? = null
    //private var normalHeadPitch: Float? = null
    //private var headDownFrames = 0
    //private var wasNodding = false
    //private var totalHeadNods = 0

    //private val headFrames = 15
    //private val headPitchThreshold = 8f
    //private val headThreshold = 30f

    private var drowsinessScore = 0
    private val eyeScore = 40
    private val yawnScore = 30
    //private val headScore = 30

    private var alarmPlayer: MediaPlayer? = null
    private var monitoringStartTime = 0L
    private var alarmActivations = 0
    private var highestRisk = "SAFE"
    private var alarmWasActive = false




    private lateinit var databaseHelper: DatabaseHelper






    private fun setupFaceLandmarker() {

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("face_landmarker.task")
            .build()

        val options =
            FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.VIDEO)
                .setNumFaces(1)
                .build()

        faceLandmarker =
            FaceLandmarker.createFromOptions(
                this,
                options
            )
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_monitoring)


        databaseHelper = DatabaseHelper(this)

        monitoringStartTime = System.currentTimeMillis()


        loadSettings()
        monitoringStartTime = System.currentTimeMillis()

        setupFaceLandmarker()

        cameraPreview = findViewById(R.id.cameraPreview)

        val stopButton =
            findViewById<Button>(R.id.stopMonitoringButton)

        stopButton.setOnClickListener {

            saveMonitoringSession()

            stopAlarm()

            val duration =
                System.currentTimeMillis() - monitoringStartTime

            val intent =
                Intent(
                    this,
                    SummaryActivity::class.java
                )

            intent.putExtra(
                "duration",
                duration
            )

            intent.putExtra(
                "drowsyEvents",
                totalDrowsyEvents
            )

            intent.putExtra(
                "yawns",
                totalYawns
            )

            intent.putExtra(
                "alarmActivations",
                alarmActivations
            )

            intent.putExtra(
                "highestRisk",
                highestRisk
            )

            startActivity(intent)

            finish()
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    private fun loadSettings() {

        val prefs =
            getSharedPreferences(
                "SafeDriveSettings",
                MODE_PRIVATE
            )

        eyeThreshold =
            prefs.getFloat(
                "eyeThreshold",
                0.20f
            )

        yawnThreshold =
            prefs.getFloat(
                "yawnThreshold",
                0.40f
            )

        alarmVolume =
            prefs.getInt(
                "alarmVolume",
                100
            )
    }




    private fun updateAlarm(score: Int) {

        if (score >= 30) {

            if (alarmPlayer == null) {

                alarmPlayer = MediaPlayer.create(
                    this,
                    R.raw.alarm
                )

                val volume =
                    alarmVolume / 100f

                alarmPlayer?.setVolume(
                    volume,
                    volume
                )

                alarmPlayer?.isLooping = true
                alarmPlayer?.start()

                alarmActivations++
                alarmWasActive = true
            }

        } else {

            stopAlarm()
            alarmWasActive = false
        }
    }


    private fun stopAlarm() {

        alarmPlayer?.stop()
        alarmPlayer?.release()
        alarmPlayer = null
    }




    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()

            preview.surfaceProvider =
                cameraPreview.surfaceProvider

            val imageAnalyzer =
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

            imageAnalyzer.setAnalyzer(
                ContextCompat.getMainExecutor(this)
            ) { imageProxy ->

                processImage(imageProxy)
            }

            val cameraSelector =
                CameraSelector.DEFAULT_FRONT_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy) {

        try {

            val bitmap = imageProxy.toBitmap()

            val mpImage =
                BitmapImageBuilder(bitmap).build()

            val timestamp =
                imageProxy.imageInfo.timestamp / 1_000_000

            val result =
                faceLandmarker.detectForVideo(
                    mpImage,
                    timestamp
                )

            runOnUiThread {

                if (result.faceLandmarks().isNotEmpty()) {

                    val landmarks = result.faceLandmarks()[0]

                    //var headNodding = false

                    // -------------------------
                    // HEAD PITCH / NODDING
                    // -------------------------

                    //val currentPitch =
                    //    calculateHeadPitch(landmarks)

                    //if (normalHeadPitch == null) {

                    //   normalHeadPitch = currentPitch
                    //}

                    //val pitchDifference =
                    //    currentPitch - normalHeadPitch!!

                    //if (pitchDifference > headPitchThreshold) {

                    //    headDownFrames++

                    //} else {

                    //    headDownFrames = 0
                    //}

                    //headNodding =
                    //    headDownFrames > headFrames

                    //if (headNodding) {

                    //    findViewById<TextView>(
                    //        R.id.headLabel
                    //    ).text = "NODDING!"

                    //    if (!wasNodding) {

                    //        totalHeadNods++
                    //        wasNodding = true
                    //    }

                    //} else {

                    //    findViewById<TextView>(
                    //        R.id.headLabel
                    //    ).text = "NORMAL"

                    //    wasNodding = false
                    //}



                    val leftEAR = calculateEAR(
                        LEFT_EYE,
                        landmarks
                    )

                    val rightEAR = calculateEAR(
                        RIGHT_EYE,
                        landmarks
                    )

                    val ear = (leftEAR + rightEAR) / 2f

                    // -------------------------
                    // MAR
                    // -------------------------

                    val mar = calculateMAR(landmarks)

                    // -------------------------
                    // EYE DROWSINESS
                    // -------------------------

                    if (ear < eyeThreshold) {

                        eyeClosedFrames++

                    } else {

                        eyeClosedFrames = 0
                    }

                    val drowsy = eyeClosedFrames > DROWSY_FRAMES

                    if (drowsy) {

                        findViewById<TextView>(R.id.statusLabel).text =
                            "DROWSY!"

                        if (!wasDrowsy) {

                            totalDrowsyEvents++
                            wasDrowsy = true
                        }

                    } else {

                        findViewById<TextView>(R.id.statusLabel).text =
                            "AWAKE"

                        wasDrowsy = false
                    }

                    // -------------------------
                    // YAWNING
                    // -------------------------

                    if (mar > yawnThreshold) {

                        yawnFrames++

                    } else {

                        yawnFrames = 0
                    }

                    val yawning = yawnFrames > YAWN_FRAMES

                    if (yawning) {

                        findViewById<TextView>(R.id.yawnLabel).text =
                            "YAWNING!"

                        if (!wasYawning) {

                            totalYawns++
                            wasYawning = true
                        }

                    } else {

                        findViewById<TextView>(R.id.yawnLabel).text =
                            "NO YAWN"

                        wasYawning = false
                    }

                    drowsinessScore = 0

                    if (drowsy) {
                        drowsinessScore += eyeScore
                    }

                    if (yawning) {
                        drowsinessScore += yawnScore
                    }

                    //if (headNodding) {
                    //    drowsinessScore += headScore
                    //}

                    findViewById<TextView>(
                        R.id.scoreLabel
                    ).text = "Score: $drowsinessScore%"


                    val riskText = when {

                        drowsinessScore >= 70 ->
                            "HIGH RISK"

                        drowsinessScore >= 40 ->
                            "MODERATE RISK"

                        else ->
                            "SAFE"
                    }

                    if (drowsinessScore >= 70) {

                        highestRisk = "HIGH RISK"

                    } else if (
                        drowsinessScore >= 40 &&
                        highestRisk != "HIGH RISK"
                    ) {

                        highestRisk = "MODERATE RISK"
                    }

                    findViewById<TextView>(
                        R.id.riskLabel
                    ).text = riskText

                    updateAlarm(drowsinessScore)

                } else {

                    findViewById<TextView>(R.id.statusLabel).text =
                        "NO FACE"
                }

            }

            mpImage.close()

        } catch (e: Exception) {

            e.printStackTrace()

        } finally {

            imageProxy.close()
        }
    }

    private fun calculateEAR(
        eyePoints: IntArray,
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>
    ): Float {

        fun distance(
            p1: com.google.mediapipe.tasks.components.containers.NormalizedLandmark,
            p2: com.google.mediapipe.tasks.components.containers.NormalizedLandmark
        ): Float {

            val dx = p1.x() - p2.x()
            val dy = p1.y() - p2.y()

            return sqrt(dx * dx + dy * dy)
        }

        val A = distance(
            landmarks[eyePoints[1]],
            landmarks[eyePoints[5]]
        )

        val B = distance(
            landmarks[eyePoints[2]],
            landmarks[eyePoints[4]]
        )

        val C = distance(
            landmarks[eyePoints[0]],
            landmarks[eyePoints[3]]
        )

        return (A + B) / (2f * C)
    }

    private fun calculateMAR(
        landmarks: List<com.google.mediapipe.tasks.components.containers.NormalizedLandmark>
    ): Float {

        val upper = landmarks[13]
        val lower = landmarks[14]
        val left = landmarks[61]
        val right = landmarks[291]

        val verticalX = upper.x() - lower.x()
        val verticalY = upper.y() - lower.y()

        val horizontalX = left.x() - right.x()
        val horizontalY = left.y() - right.y()

        val vertical =
            sqrt(verticalX * verticalX + verticalY * verticalY)

        val horizontal =
            sqrt(horizontalX * horizontalX + horizontalY * horizontalY)

        return vertical / horizontal
    }


    private fun saveMonitoringSession() {

        val duration =
            System.currentTimeMillis() - monitoringStartTime

        databaseHelper.insertMonitoringSession(

            date = System.currentTimeMillis(),

            duration = duration,

            drowsyEvents = totalDrowsyEvents,

            yawns = totalYawns,

            alarms = alarmActivations,

            highestRisk = highestRisk
        )
    }



    override fun onDestroy() {

        stopAlarm()

        super.onDestroy()
    }


}