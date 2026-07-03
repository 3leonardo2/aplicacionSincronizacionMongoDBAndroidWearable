package com.example.SincronizacionApp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
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
    val listState = rememberScalingLazyListState()
    val healthState by viewModel.healthState.collectAsState()
    
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(
            top = 32.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 32.dp
        )
    ) {
        item {
            Text(
                text = "ESTADO DE SALUD",
                style = MaterialTheme.typography.labelMedium,
                color = BlueGradient,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            HealthDataItem(
                label = "BPM", 
                value = if (healthState.bpm > 0) healthState.bpm.toString() else "--", 
                unit = " lpm"
            )
        }
        item {
            HealthDataItem(
                label = "Presión", 
                value = String.format(Locale.US, "%.2f", healthState.pressure), 
                unit = " kPa"
            )
        }
        item {
            HealthDataItem(
                label = "Temp", 
                value = String.format(Locale.US, "%.1f", healthState.temp), 
                unit = " °C"
            )
        }
        item {
            HealthDataItem(
                label = "Humedad", 
                value = String.format(Locale.US, "%.1f", healthState.humidity), 
                unit = " %"
            )
        }
        item {
            HealthDataItem(
                label = "Luz", 
                value = String.format(Locale.US, "%.0f", healthState.light),
                unit = " lx"
            )
        }
        item {
            HealthDataItem(
                label = "SpO2", 
                value = healthState.spo2.toInt().toString(), 
                unit = " %"
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = { viewModel.syncToMobile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3), // Azul estándar para visibilidad
                    contentColor = Color.White
                )
            ) {
                Text("Sincronizar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HealthDataItem(label: String, value: String, unit: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = { /* No-op */ },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text("$value$unit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
