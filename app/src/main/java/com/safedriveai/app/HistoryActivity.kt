package com.safedriveai.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_history
        )

        databaseHelper =
            DatabaseHelper(this)

        val recyclerView =
            findViewById<RecyclerView>(
                R.id.historyRecyclerView
            )

        val emptyLabel =
            findViewById<TextView>(
                R.id.emptyHistoryLabel
            )

        val backButton =
            findViewById<Button>(
                R.id.backButton
            )

        val sessions =
            databaseHelper.getAllMonitoringSessions()

        if (sessions.isEmpty()) {

            emptyLabel.visibility =
                TextView.VISIBLE

        } else {

            emptyLabel.visibility =
                TextView.GONE

            recyclerView.layoutManager =
                LinearLayoutManager(this)

            recyclerView.adapter =
                HistoryAdapter(sessions)
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}