package com.safedriveai.app

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var eyeSeekBar: SeekBar
    private lateinit var yawnSeekBar: SeekBar
    private lateinit var volumeSeekBar: SeekBar

    private lateinit var eyeValueLabel: TextView
    private lateinit var yawnValueLabel: TextView
    private lateinit var volumeValueLabel: TextView

    private val prefsName = "SafeDriveSettings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        eyeSeekBar = findViewById(R.id.eyeSeekBar)
        yawnSeekBar = findViewById(R.id.yawnSeekBar)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)

        eyeValueLabel = findViewById(R.id.eyeValueLabel)
        yawnValueLabel = findViewById(R.id.yawnValueLabel)
        volumeValueLabel = findViewById(R.id.volumeValueLabel)

        val saveButton =
            findViewById<Button>(R.id.saveSettingsButton)

        val prefs =
            getSharedPreferences(
                prefsName,
                Context.MODE_PRIVATE
            )

        // Load previously saved settings

        val savedEye =
            prefs.getFloat("eyeThreshold", 0.20f)

        val savedYawn =
            prefs.getFloat("yawnThreshold", 0.40f)

        val savedVolume =
            prefs.getInt("alarmVolume", 100)

        eyeSeekBar.progress =
            ((savedEye - 0.10f) * 100).toInt()

        yawnSeekBar.progress =
            ((savedYawn - 0.30f) * 100).toInt()

        volumeSeekBar.progress =
            savedVolume

        updateEyeLabel()
        updateYawnLabel()
        updateVolumeLabel()

        eyeSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    updateEyeLabel()
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        yawnSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    updateYawnLabel()
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        volumeSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    updateVolumeLabel()
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        saveButton.setOnClickListener {

            val eyeThreshold =
                0.10f +
                        eyeSeekBar.progress / 100f

            val yawnThreshold =
                0.30f +
                        yawnSeekBar.progress / 100f

            val alarmVolume =
                volumeSeekBar.progress

            prefs.edit()
                .putFloat(
                    "eyeThreshold",
                    eyeThreshold
                )
                .putFloat(
                    "yawnThreshold",
                    yawnThreshold
                )
                .putInt(
                    "alarmVolume",
                    alarmVolume
                )
                .apply()

            finish()
        }
    }

    private fun updateEyeLabel() {

        val value =
            0.10f +
                    eyeSeekBar.progress / 100f

        eyeValueLabel.text =
            "EAR Threshold: %.2f".format(value)
    }

    private fun updateYawnLabel() {

        val value =
            0.30f +
                    yawnSeekBar.progress / 100f

        yawnValueLabel.text =
            "MAR Threshold: %.2f".format(value)
    }

    private fun updateVolumeLabel() {

        volumeValueLabel.text =
            "Volume: ${volumeSeekBar.progress}%"
    }
}