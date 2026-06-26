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
                Log.d("HealthManager", "BPM Availability: $availability")
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRates = data.getData(dataType)
                if (heartRates.isNotEmpty()) {
                    trySend(heartRates.last().value.toInt())
                }
            }
        }
        
        // También registrar listener tradicional como fallback para el emulador
        val hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val hrListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { trySend(it.values[0].toInt()) }
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

    fun spo2MeasureFlow(): Flow<Float> = callbackFlow {
        trySend(98f)
        awaitClose { }
    }

    suspend fun getPressureKpa(): Float = 0f
}
