package com.example.SincronizacionApp.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.SincronizacionApp.data.HealthServicesManager
import com.example.SincronizacionApp.data.HealthSyncCoordinator
import com.example.SincronizacionApp.data.WearDataManager
import kotlinx.coroutines.flow.StateFlow
import com.example.SincronizacionApp.data.HealthState

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val wearDataManager = WearDataManager(application)
    private val healthManager = HealthServicesManager(application)
    
    private val syncCoordinator = HealthSyncCoordinator(healthManager, wearDataManager)

    // Exponemos el estado de salud al UI
    val healthState: StateFlow<HealthState> = syncCoordinator.currentHealthState

    init {
        // Inicializamos la escucha de mensajes y sensores
        wearDataManager.register()
        syncCoordinator.startTracking()
    }

    // Función que llamará el botón "Sincronizar" en el reloj
    fun syncToMobile() {
        syncCoordinator.syncNow()
    }

    override fun onCleared() {
        super.onCleared()
        wearDataManager.unregister()
    }
}
