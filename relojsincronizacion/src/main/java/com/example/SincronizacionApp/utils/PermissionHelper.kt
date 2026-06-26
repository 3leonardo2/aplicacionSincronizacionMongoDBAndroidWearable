package com.example.SincronizacionApp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat

object PermissionHelper {
    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    fun hasPermissions(context: Context): Boolean {
        return permissions.all {
            val granted = ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            android.util.Log.d("PermissionHelper", "Permission: $it granted: $granted")
            granted
        }
    }
}

@Composable
fun RequestHealthPermissions(onPermissionsGranted: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        android.util.Log.d("PermissionHelper", "Launcher result: $permissions")
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onPermissionsGranted()
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
