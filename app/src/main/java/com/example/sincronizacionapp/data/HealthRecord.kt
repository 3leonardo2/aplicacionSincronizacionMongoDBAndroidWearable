package com.example.sincronizacionapp.data

data class HealthRecord(
    val bpm: Int,
    val pressure: Float,
    val temperature: Float,
    val humidity: Float,
    val light: Float,
    val spo2: Float,
    val deviceName: String = "Physical-Mobile"
)
