package com.example.SincronizacionApp.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class HealthServicesManager(context: Context) {
    private val healthServicesClient = HealthServices.getClient(context)
    private val measureClient = healthServicesClient.measureClient
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Flow para el ritmo cardíaco (BPM) con fallback a SensorManager
    fun heartRateMeasureFlow(): Flow<Int> = callbackFlow {
        // Intentar primero con Health Services
        val dataType = DataType.HEART_RATE_BPM
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                Log.d("HealthManager", "BPM Availability (HealthServices): $availability")
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRates = data.getData(dataType)
                Log.d("HealthManager", "BPM Data Received (HealthServices): $heartRates")
                if (heartRates.isNotEmpty()) {
                    val bpm = heartRates.last().value.toInt()
                    Log.d("HealthManager", "BPM Value (HealthServices): $bpm")
                    trySend(bpm)
                }
            }
        }
        
        // También registrar listener tradicional como fallback para el emulador
        val hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        Log.d("HealthManager", "HR Sensor (Fallback): ${hrSensor?.name ?: "No detectado"}")
        
        val hrListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { 
                    val bpm = it.values[0].toInt()
                    Log.d("HealthManager", "BPM Value (SensorManager Fallback): $bpm")
                    trySend(bpm) 
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        measureClient.registerMeasureCallback(dataType, callback)
        if (hrSensor != null) {
            sensorManager.registerListener(hrListener, hrSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        awaitClose { 
            measureClient.unregisterMeasureCallbackAsync(dataType, callback)
            sensorManager.unregisterListener(hrListener)
        }
    }

    private fun createSensorFlow(sensorType: Int): Flow<Float> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { trySend(it.values[0]) }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    fun pressureMeasureFlow(): Flow<Float> = createSensorFlow(Sensor.TYPE_PRESSURE)
    fun temperatureMeasureFlow(): Flow<Float> = createSensorFlow(Sensor.TYPE_AMBIENT_TEMPERATURE)
    fun humidityMeasureFlow(): Flow<Float> = createSensorFlow(Sensor.TYPE_RELATIVE_HUMIDITY)
    fun lightMeasureFlow(): Flow<Float> = createSensorFlow(Sensor.TYPE_LIGHT)

    // Flow para pasos (Steps)
    fun stepsMeasureFlow(): Flow<Int> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val listener = object : SensorEventListener {
            private var initialSteps = -1
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val currentSteps = it.values[0].toInt()
                    if (initialSteps == -1) initialSteps = currentSteps
                    trySend(currentSteps - initialSteps)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    // Flow para calorías (Simulado basado en pasos si no hay sensor específico)
    fun caloriesMeasureFlow(): Flow<Int> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val listener = object : SensorEventListener {
            private var initialSteps = -1
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val currentSteps = it.values[0].toInt()
                    if (initialSteps == -1) initialSteps = currentSteps
                    val steps = currentSteps - initialSteps
                    // Aproximación simple: 0.04 calorías por paso
                    trySend((steps * 0.04f).toInt())
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensor != null) {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            trySend(0)
        }
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    fun spo2MeasureFlow(): Flow<Float> = callbackFlow {
        trySend(98f)
        awaitClose { }
    }

    suspend fun getPressureKpa(): Float = 0f
}
