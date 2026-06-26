package com.example.sincronizacionapp.data

import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sincronizacionapp.R
import com.example.sincronizacionapp.api.HealthApiService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val wearDataManager = WearDataManager(application)
    private val apiService = HealthApiService.create()
    
    val healthData: StateFlow<Map<String, Any>> = wearDataManager.healthData

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    init {
        wearDataManager.register()
    }

    fun syncFromWatch() {
        viewModelScope.launch {
            wearDataManager.requestSync()
        }
    }

    fun uploadToCloud() {
        val currentData = healthData.value
        if (currentData.isEmpty()) {
            Log.w("MainViewModel", "No hay datos para subir")
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowSnackbar("No hay datos para subir")) }
            return
        }

        viewModelScope.launch {
            try {
                val record = HealthRecord(
                    bpm = (currentData[WearConstants.KEY_BPM] as? Int) ?: 0,
                    pressure = (currentData[WearConstants.KEY_PRESSURE] as? Float) ?: 0f,
                    temperature = (currentData[WearConstants.KEY_TEMP] as? Float) ?: 0f,
                    humidity = (currentData[WearConstants.KEY_HUMIDITY] as? Float) ?: 0f,
                    light = (currentData[WearConstants.KEY_LIGHT] as? Float) ?: 0f,
                    spo2 = (currentData[WearConstants.KEY_SPO2] as? Float) ?: 98f
                )

                val response = apiService.uploadHealthData(record)
                if (response.isSuccessful) {
                    Log.d("MainViewModel", "Datos subidos con éxito: ${response.body()}")
                    _uiEvents.emit(UiEvent.ShowSnackbar("¡Datos subidos con éxito a MongoDB!"))
                    playNotificationSound(true)
                } else {
                    val errorMsg = "Error al subir datos: ${response.code()}"
                    Log.e("MainViewModel", errorMsg)
                    _uiEvents.emit(UiEvent.ShowSnackbar(errorMsg))
                    playNotificationSound(false)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Fallo de conexión al subir a la nube", e)
                _uiEvents.emit(UiEvent.ShowSnackbar("Fallo de conexión: ${e.message}"))
                playNotificationSound(false)
            }
        }
    }

    private fun playNotificationSound(isSuccess: Boolean) {
        try {
            // Utilizamos tus sonidos personalizados exito.mp3 y error.mp3
            val soundRes = if (isSuccess) R.raw.exito else R.raw.error
            val mp = MediaPlayer.create(getApplication(), soundRes)
            
            if (mp != null) {
                mp.start()
                mp.setOnCompletionListener { it.release() }
            } else {
                Log.e("MainViewModel", "No se pudo cargar el recurso de audio")
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error al reproducir sonido personalizado", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        wearDataManager.unregister()
    }
}
