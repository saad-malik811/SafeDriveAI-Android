package com.safedriveai.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_summary)

        val durationLabel =
            findViewById<TextView>(
                R.id.durationLabel
            )

        val drowsyEventsLabel =
            findViewById<TextView>(
                R.id.drowsyEventsLabel
            )

        val yawnsLabel =
            findViewById<TextView>(
                R.id.yawnsLabel
            )

        val alarmEventsLabel =
            findViewById<TextView>(
                R.id.alarmEventsLabel
            )

        val highestRiskLabel =
            findViewById<TextView>(
                R.id.highestRiskLabel
            )

        val doneButton =
            findViewById<Button>(
                R.id.doneButton
            )

        val duration =
            intent.getLongExtra(
                "duration",
                0L
            )

        val drowsyEvents =
            intent.getIntExtra(
                "drowsyEvents",
                0
            )

        val yawns =
            intent.getIntExtra(
                "yawns",
                0
            )

        val alarmActivations =
            intent.getIntExtra(
                "alarmActivations",
                0
            )

        val highestRisk =
            intent.getStringExtra(
                "highestRisk"
            ) ?: "SAFE"

        val totalSeconds =
            duration / 1000

        val minutes =
            totalSeconds / 60

        val seconds =
            totalSeconds % 60

        durationLabel.text =
            "Duration: %02d:%02d".format(
                minutes,
                seconds
            )

        drowsyEventsLabel.text =
            "Drowsy Events: $drowsyEvents"

        yawnsLabel.text =
            "Yawns Detected: $yawns"

        alarmEventsLabel.text =
            "Alarm Activations: $alarmActivations"

        highestRiskLabel.text =
            "Highest Risk: $highestRisk"

        doneButton.setOnClickListener {

            finish()
        }
    }
}