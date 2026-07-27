package com.example.SincronizacionApp.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.coroutines.tasks.await
import java.util.Date

class WearDataManager(context: Context) : 
    MessageClient.OnMessageReceivedListener,
    DataClient.OnDataChangedListener {

    private val dataClient = Wearable.getDataClient(context)
    private val messageClient = Wearable.getMessageClient(context)
    
    var onSyncRequestReceived: (() -> Unit)? = null
    var onNutritionDataReceived: ((DataMap) -> Unit)? = null

    fun register() {
        messageClient.addListener(this)
        dataClient.addListener(this)
    }

    fun unregister() {
        messageClient.removeListener(this)
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == WearConstants.NUTRITION_PATH) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                onNutritionDataReceived?.invoke(dataMap)
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == WearConstants.SYNC_PATH) {
            Log.d("WearDataManager", "Sync request received from mobile")
            onSyncRequestReceived?.invoke()
        }
    }

    suspend fun sendHealthData(
        bpm: Int, 
        pressure: Float, 
        spo2: Float, 
        temp: Float = 0f, 
        humidity: Float = 0f, 
        light: Float = 0f
    ) {
        try {
            val request = PutDataMapRequest.create(WearConstants.HEALTH_PATH).apply {
                dataMap.putInt(WearConstants.KEY_BPM, bpm)
                dataMap.putFloat(WearConstants.KEY_PRESSURE, pressure)
                dataMap.putFloat(WearConstants.KEY_SPO2, spo2)
                dataMap.putFloat(WearConstants.KEY_TEMP, temp)
                dataMap.putFloat(WearConstants.KEY_HUMIDITY, humidity)
                dataMap.putFloat(WearConstants.KEY_LIGHT, light)
                dataMap.putLong(WearConstants.KEY_TIMESTAMP, Date().time)
                setUrgent()
            }.asPutDataRequest()
            
            dataClient.putDataItem(request).await()
            Log.d("WearDataManager", "Data sent successfully to mobile")
        } catch (e: Exception) {
            Log.e("WearDataManager", "Error sending data", e)
        }
    }
}
