package com.example.SincronizacionApp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.*
import com.example.SincronizacionApp.presentation.theme.*
import com.example.SincronizacionApp.utils.RequestHealthPermissions
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp(mainViewModel: MainViewModel = viewModel()) {
    var permissionsGranted by remember { mutableStateOf(false) }

    SincronizacionAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            RequestHealthPermissions {
                permissionsGranted = true
            }
            if (!permissionsGranted) {
                Text("Esperando permisos...", color = Color.Gray, fontSize = 12.sp)
            } else {
                WatchScreen(mainViewModel)
            }
        }
    }
}

@Composable
fun WatchScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    // Observamos el estado de salud del ViewModel
    val healthState by viewModel.healthState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "ESTADO DE SALUD",
            style = MaterialTheme.typography.labelSmall,
            color = BlueGradient
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Mostramos los valores REALES del healthState
        HealthDataItem(
            label = "BPM", 
            value = if (healthState.bpm > 0) healthState.bpm.toString() else "--", 
            unit = " lpm"
        )
        HealthDataItem(
            label = "Presión", 
            value = String.format(Locale.US, "%.2f", healthState.pressure), 
            unit = " kPa"
        )
        HealthDataItem(
            label = "Temp", 
            value = String.format(Locale.US, "%.1f", healthState.temp), 
            unit = " °C"
        )
        HealthDataItem(
            label = "Humedad", 
            value = String.format(Locale.US, "%.1f", healthState.humidity), 
            unit = " %"
        )
        HealthDataItem(
            label = "Luz", 
            value = String.format(Locale.US, "%.0f", healthState.light),
            unit = " lx"
        )
        HealthDataItem(
            label = "SpO2", 
            value = healthState.spo2.toInt().toString(), 
            unit = " %"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.syncToMobile() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(ButtonGradient, shape = RoundedCornerShape(24.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        ) {
            Text("Sincronizar", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HealthDataItem(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.LightGray, fontSize = 14.sp)
        Text("$value$unit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
