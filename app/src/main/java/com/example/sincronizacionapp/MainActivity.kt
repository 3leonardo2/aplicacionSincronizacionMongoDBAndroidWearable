package com.example.sincronizacionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sincronizacionapp.data.MainViewModel
import com.example.sincronizacionapp.data.WearConstants
import com.example.sincronizacionapp.ui.theme.*
import com.example.sincronizacionapp.utils.RequestMobilePermissions
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SincronizacionAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf("main") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is MainViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    "main" -> MainGridScreen(
                        viewModel = viewModel,
                        onNavigateToHistory = { currentScreen = "history" }
                    )
                    "history" -> HistoryScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "main" }
                    )
                }
            }
        }
    }
}

@Composable
fun MainGridScreen(viewModel: MainViewModel, onNavigateToHistory: () -> Unit) {
    var permissionsGranted by remember { mutableStateOf(false) }
    val healthData by viewModel.healthData.collectAsState()

    if (!permissionsGranted) {
        RequestMobilePermissions { permissionsGranted = true }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se requieren permisos de sensores", color = Color.Gray)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mi Salud",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier.background(CardBackground, CircleShape)
                ) {
                    Icon(Icons.Default.History, contentDescription = "Historial", tint = Color.White)
                }
            }

            // Grid de Bloques (3 filas x 2 columnas)
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.weight(1f)) {
                    HealthBlock(
                        title = "Ritmo Cardíaco",
                        value = healthData[WearConstants.KEY_BPM]?.toString() ?: "--",
                        unit = "BPM",
                        icon = "❤️",
                        modifier = Modifier.weight(1f)
                    )
                    HealthBlock(
                        title = "Presión Atm.",
                        value = String.format("%.1f", healthData[WearConstants.KEY_PRESSURE] ?: 0.0),
                        unit = "kPa",
                        icon = "🌡️",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.weight(1f)) {
                    HealthBlock(
                        title = "Temperatura",
                        value = String.format("%.1f", healthData[WearConstants.KEY_TEMP] ?: 0.0),
                        unit = "°C",
                        icon = "🔥",
                        modifier = Modifier.weight(1f)
                    )
                    HealthBlock(
                        title = "Humedad",
                        value = String.format("%.1f", healthData[WearConstants.KEY_HUMIDITY] ?: 0.0),
                        unit = "%",
                        icon = "💧",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.weight(1f)) {
                    HealthBlock(
                        title = "Luz Ambiente",
                        value = String.format("%.0f", healthData[WearConstants.KEY_LIGHT] ?: 0.0),
                        unit = "lux",
                        icon = "☀️",
                        modifier = Modifier.weight(1f)
                    )
                    HealthBlock(
                        title = "Oxígeno",
                        value = healthData[WearConstants.KEY_SPO2]?.toString() ?: "--",
                        unit = "%",
                        icon = "🩸",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.syncFromWatch() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizar", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { viewModel.uploadToCloud() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Subir", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun HistoryScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val lastRecord by viewModel.lastCloudRecord.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.fetchLastRecord()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
            Text(
                text = "Último Registro Cloud",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (lastRecord == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlueGradient)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Datos en MongoDB Atlas",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HistoryItem("Ritmo Cardíaco", "${lastRecord?.get("bpm")} BPM")
                    HistoryItem("Presión", "${lastRecord?.get("pressure")} kPa")
                    HistoryItem("Temperatura", "${lastRecord?.get("temperature")} °C")
                    HistoryItem("Humedad", "${lastRecord?.get("humidity")} %")
                    HistoryItem("Luz", "${lastRecord?.get("light")} lux")
                    HistoryItem("Oxígeno (SpO2)", "${lastRecord?.get("spo2")} %")
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.DarkGray)
                    
                    Text(
                        text = "Fecha: ${lastRecord?.get("timestamp")}",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { viewModel.fetchLastRecord() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueGradient)
            ) {
                Text("Actualizar Historial", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HealthBlock(title: String, value: String, unit: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(6.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = icon, fontSize = 28.sp)
            Column {
                Text(text = title, color = Color.Gray, fontSize = 12.sp, lineHeight = 14.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = unit,
                        color = BlueGradient,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 15.sp)
        Text(text = value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
