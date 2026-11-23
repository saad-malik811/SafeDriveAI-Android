package com.safedriveai.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION
    ) {

    companion object {

        private const val DATABASE_NAME =
            "safedrive_history.db"

        private const val DATABASE_VERSION = 1

        const val TABLE_HISTORY =
            "monitoring_history"

        const val COLUMN_ID =
            "id"

        const val COLUMN_DATE =
            "date"

        const val COLUMN_DURATION =
            "duration"

        const val COLUMN_DROWSY =
            "drowsy_events"

        const val COLUMN_YAWNS =
            "yawns"

        const val COLUMN_ALARMS =
            "alarm_activations"

        const val COLUMN_RISK =
            "highest_risk"
    }

    override fun onCreate(db: SQLiteDatabase) {

        val createTable = """
            CREATE TABLE $TABLE_HISTORY (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE INTEGER NOT NULL,
                $COLUMN_DURATION INTEGER NOT NULL,
                $COLUMN_DROWSY INTEGER NOT NULL,
                $COLUMN_YAWNS INTEGER NOT NULL,
                $COLUMN_ALARMS INTEGER NOT NULL,
                $COLUMN_RISK TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        db.execSQL(
            "DROP TABLE IF EXISTS $TABLE_HISTORY"
        )

        onCreate(db)
    }

    fun insertMonitoringSession(
        date: Long,
        duration: Long,
        drowsyEvents: Int,
        yawns: Int,
        alarms: Int,
        highestRisk: String
    ) {

        val db = writableDatabase

        val values = ContentValues()

        values.put(
            COLUMN_DATE,
            date
        )

        values.put(
            COLUMN_DURATION,
            duration
        )

        values.put(
            COLUMN_DROWSY,
            drowsyEvents
        )

        values.put(
            COLUMN_YAWNS,
            yawns
        )

        values.put(
            COLUMN_ALARMS,
            alarms
        )

        values.put(
            COLUMN_RISK,
            highestRisk
        )

        db.insert(
            TABLE_HISTORY,
            null,
            values
        )
    }

    fun getAllMonitoringSessions(): List<MonitoringSession> {

        val sessions = mutableListOf<MonitoringSession>()

        val db = readableDatabase

        val cursor = db.query(
            TABLE_HISTORY,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE DESC"
        )

        while (cursor.moveToNext()) {

            val id =
                cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_ID
                    )
                )

            val date =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_DATE
                    )
                )

            val duration =
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_DURATION
                    )
                )

            val drowsyEvents =
                cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_DROWSY
                    )
                )

            val yawns =
                cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_YAWNS
                    )
                )

            val alarms =
                cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_ALARMS
                    )
                )

            val highestRisk =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_RISK
                    )
                )

            sessions.add(
                MonitoringSession(
                    id = id,
                    date = date,
                    duration = duration,
                    drowsyEvents = drowsyEvents,
                    yawns = yawns,
                    alarms = alarms,
                    highestRisk = highestRisk
                )
            )
        }

        cursor.close()

        return sessions
    }
}