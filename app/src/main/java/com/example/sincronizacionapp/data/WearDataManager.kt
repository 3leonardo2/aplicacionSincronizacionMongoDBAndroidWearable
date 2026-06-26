package com.example.sincronizacionapp.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WearDataManager(context: Context) : DataClient.OnDataChangedListener {

    private val dataClient = Wearable.getDataClient(context)
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)

    private val _healthData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val healthData = _healthData.asStateFlow()

    fun register() {
        dataClient.addListener(this)
    }

    fun unregister() {
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == WearConstants.HEALTH_PATH) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val newData = mapOf(
                    WearConstants.KEY_BPM to dataMap.getInt(WearConstants.KEY_BPM),
                    WearConstants.KEY_PRESSURE to dataMap.getFloat(WearConstants.KEY_PRESSURE),
                    WearConstants.KEY_SPO2 to dataMap.getFloat(WearConstants.KEY_SPO2),
                    WearConstants.KEY_TEMP to dataMap.getFloat(WearConstants.KEY_TEMP),
                    WearConstants.KEY_HUMIDITY to dataMap.getFloat(WearConstants.KEY_HUMIDITY),
                    WearConstants.KEY_LIGHT to dataMap.getFloat(WearConstants.KEY_LIGHT),
                    WearConstants.KEY_TIMESTAMP to dataMap.getLong(WearConstants.KEY_TIMESTAMP)
                )
                _healthData.value = newData
                Log.d("WearDataManager", "Data received: $newData")
            }
        }
    }

    suspend fun requestSync() {
        try {
            val nodes = nodeClient.connectedNodes.await()
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, WearConstants.SYNC_PATH, byteArrayOf()).await()
            }
            Log.d("WearDataManager", "Sync request sent to ${nodes.size} nodes")
        } catch (e: Exception) {
            Log.e("WearDataManager", "Error requesting sync", e)
        }
    }
}
