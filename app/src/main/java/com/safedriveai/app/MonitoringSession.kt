package com.safedriveai.app

data class MonitoringSession(

    val id: Int,

    val date: Long,

    val duration: Long,

    val drowsyEvents: Int,

    val yawns: Int,

    val alarms: Int,

    val highestRisk: String
)