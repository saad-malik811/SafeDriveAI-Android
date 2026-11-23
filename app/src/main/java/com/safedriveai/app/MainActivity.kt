package com.safedriveai.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.btnStartMonitoring)

        startButton.setOnClickListener {
            val intent = Intent(this, MonitoringActivity::class.java)
            startActivity(intent)
        }
        val settingsButton = findViewById<Button>(R.id.btnSettings)

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val historyButton = findViewById<Button>(R.id.btnHistory)

        val aboutButton =
            findViewById<Button>(R.id.btnAbout)

        aboutButton.setOnClickListener {

            val intent =
                Intent(
                    this,
                    AboutActivity::class.java
                )

            startActivity(intent)
        }

        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
}