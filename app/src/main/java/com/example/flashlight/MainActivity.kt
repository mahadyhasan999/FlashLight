package com.example.flashlight

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var isFlashlightOn = false
    private var isStrobeOn = false
    private var isSOSOn = false
    private lateinit var handler: Handler
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]
        handler = Handler(Looper.getMainLooper())

        val timerEditText = findViewById<EditText>(R.id.timerEditText)
        val flashlightButton = findViewById<Button>(R.id.flashlightButton)
        val strobeButton = findViewById<Button>(R.id.strobeButton)
        val sosButton = findViewById<Button>(R.id.sosButton)

        flashlightButton.setOnClickListener {
            val timer = timerEditText.text.toString().toIntOrNull()
            toggleFlashlight(timer)
        }

        strobeButton.setOnClickListener {
            val timer = timerEditText.text.toString().toIntOrNull()
            if (isStrobeOn) {
                stopStrobeLight()
            } else {
                startStrobeLight(timer)
            }
        }

        sosButton.setOnClickListener {
            val timer = timerEditText.text.toString().toIntOrNull()
            if (isSOSOn) {
                stopSOS()
            } else {
                startSOS(timer)
            }
        }
    }

//    @SuppressLint("ObsoleteSdkInt")
    private fun toggleFlashlight(timer: Int? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (isFlashlightOn) {
                    cameraManager.setTorchMode(cameraId, false)
                    isFlashlightOn = false
                    findViewById<Button>(R.id.flashlightButton).text = "Turn On Flashlight"
                } else {
                    cameraManager.setTorchMode(cameraId, true)
                    isFlashlightOn = true
                    findViewById<Button>(R.id.flashlightButton).text = "Turn Off Flashlight"
                    timer?.let {
                        handler.postDelayed({ toggleFlashlight() }, it * 1000L)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error toggling flashlight", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startStrobeLight(timer: Int? = null) {
        isStrobeOn = true
        val strobeButton = findViewById<Button>(R.id.strobeButton)
        strobeButton.text = "Stop Strobe Light"
        strobeRunnable.run()
        timer?.let {
            handler.postDelayed({ stopStrobeLight() }, it * 1000L)
        }
    }

    private fun stopStrobeLight() {
        isStrobeOn = false
        val strobeButton = findViewById<Button>(R.id.strobeButton)
        strobeButton.text = "Strobe Light"
        handler.removeCallbacks(strobeRunnable)
        if (isFlashlightOn) {
            toggleFlashlight()
        }
    }

    private val strobeRunnable: Runnable = object : Runnable {
        override fun run() {
            if (isStrobeOn) {
                toggleFlashlight()
                handler.postDelayed(this, 100)
            }
        }
    }

    private fun startSOS(timer: Int? = null) {
        isSOSOn = true
        val sosButton = findViewById<Button>(R.id.sosButton)
        sosButton.text = "Stop SOS"
        sosRunnable.run()
        timer?.let {
            handler.postDelayed({ stopSOS() }, it * 1000L)
        }
    }

    private fun stopSOS() {
        isSOSOn = false
        val sosButton = findViewById<Button>(R.id.sosButton)
        sosButton.text = "SOS"
        handler.removeCallbacks(sosRunnable)
        if (isFlashlightOn) {
            toggleFlashlight()
        }
    }

    private val sosRunnable: Runnable = object : Runnable {
        private val sosPattern = intArrayOf(300, 300, 300, 300, 300, 300, 600, 600, 600, 300, 300, 300, 900, 900, 900, 300, 300, 300)
        private var sosIndex = 0

        override fun run() {
            if (isSOSOn) {
                if (sosIndex < sosPattern.size) {
                    toggleFlashlight()
                    handler.postDelayed(this, sosPattern[sosIndex].toLong())
                    sosIndex++
                } else {
                    sosIndex = 0
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }
}
