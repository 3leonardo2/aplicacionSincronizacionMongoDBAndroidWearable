package com.example.SincronizacionApp.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HealthState(
    val bpm: Int = 0,
    val pressure: Float = 0f,
    val temp: Float = 0f,
    val humidity: Float = 0f,
    val light: Float = 0f,
    val spo2: Float = 98f
)

class HealthSyncCoordinator(
    private val sensorManager: HealthServicesManager,
    private val dataManager: WearDataManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _currentHealthState = MutableStateFlow(HealthState())
    val currentHealthState = _currentHealthState.asStateFlow()

    fun startTracking() {
        // 1. Recolectar datos de sensores y actualizar el estado
        scope.launch { 
            sensorManager.heartRateMeasureFlow().collectLatest { bpm ->
                Log.d("SyncCoordinator", "Recibido BPM en Flow: $bpm")
                _currentHealthState.value = _currentHealthState.value.copy(bpm = bpm)
            } 
        }
        scope.launch { 
            sensorManager.pressureMeasureFlow().collectLatest { pressure ->
                _currentHealthState.value = _currentHealthState.value.copy(pressure = pressure / 10f)
            } 
        }
        scope.launch { 
            sensorManager.temperatureMeasureFlow().collectLatest { temp ->
                // Filtro para evitar valores basura del emulador
                if (temp > -100 && temp < 100) {
                    _currentHealthState.value = _currentHealthState.value.copy(temp = temp)
                }
            } 
        }
        scope.launch { 
            sensorManager.humidityMeasureFlow().collectLatest { humidity ->
                _currentHealthState.value = _currentHealthState.value.copy(humidity = humidity)
            } 
        }
        scope.launch { 
            sensorManager.lightMeasureFlow().collectLatest { light ->
                _currentHealthState.value = _currentHealthState.value.copy(light = light)
            } 
        }
        scope.launch { 
            sensorManager.spo2MeasureFlow().collectLatest { spo2 ->
                _currentHealthState.value = _currentHealthState.value.copy(spo2 = spo2)
            } 
        }

        // 2. Logcat periódico cada 3 segundos
        scope.launch {
            while (true) {
                val s = _currentHealthState.value
                Log.d("SyncCoordinator", "--- MEDICIÓN SENSORES (cada 3s) ---")
                Log.d("SyncCoordinator", "❤️ BPM: ${s.bpm}")
                Log.d("SyncCoordinator", "🌡️ Presión: ${String.format("%.2f", s.pressure)} kPa")
                Log.d("SyncCoordinator", "🔥 Temp: ${s.temp} °C")
                Log.d("SyncCoordinator", "💧 Humedad: ${s.humidity} %")
                Log.d("SyncCoordinator", "☀️ Luz: ${s.light} lux")
                Log.d("SyncCoordinator", "----------------------------------")
                delay(3000)
            }
        }

        dataManager.onSyncRequestReceived = {
            syncNow()
        }
    }

    fun syncNow() {
        scope.launch {
            val s = _currentHealthState.value
            Log.d("SyncCoordinator", "Enviando datos al móvil...")
            dataManager.sendHealthData(
                bpm = s.bpm,
                pressure = s.pressure,
                spo2 = s.spo2,
                temp = s.temp,
                humidity = s.humidity,
                light = s.light
            )
        }
    }
}
