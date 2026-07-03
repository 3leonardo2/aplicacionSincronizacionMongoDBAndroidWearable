package com.example.SincronizacionApp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat

object PermissionHelper {
    val permissions: Array<String>
        get() {
            val list = mutableListOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
            // Agregamos READ_HEART_RATE solo si la versión es >= Android 13 (API 33)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list.add("android.permission.health.READ_HEART_RATE")
            }
            return list.toTypedArray()
        }

    fun hasPermissions(context: Context): Boolean {
        return permissions.all {
            val granted = ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            android.util.Log.d("PermissionHelper", "Checking Permission: $it granted: $granted")
            granted
        }
    }
}

@Composable
fun RequestHealthPermissions(onPermissionsGranted: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        android.util.Log.d("PermissionHelper", "Launcher result: $permissionsResult")
        
        // Verificamos si los permisos críticos están concedidos
        // En Wear OS, a veces es mejor ser menos estricto si uno falla pero los principales están
        val bodySensorsGranted = permissionsResult[Manifest.permission.BODY_SENSORS] == true
        
        if (bodySensorsGranted) {
            android.util.Log.d("PermissionHelper", "Body sensors granted, proceeding...")
            onPermissionsGranted()
        } else {
            android.util.Log.e("PermissionHelper", "Critical permissions NOT granted")
        }
    }

    LaunchedEffect(Unit) {
        val alreadyHas = PermissionHelper.hasPermissions(context)
        android.util.Log.d("PermissionHelper", "Initially has permissions: $alreadyHas")
        if (alreadyHas) {
            onPermissionsGranted()
        } else {
            launcher.launch(PermissionHelper.permissions)
        }
    }
}
