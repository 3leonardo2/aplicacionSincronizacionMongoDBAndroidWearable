package com.example.sincronizacionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    var permissionsGranted by remember { mutableStateOf(false) }
    val healthData by viewModel.healthData.collectAsState()
    val scrollState = rememberScrollState()
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
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = DarkBackground
        ) {
            if (!permissionsGranted) {
                RequestMobilePermissions {
                    permissionsGranted = true
                }
                Box(contentAlignment = Alignment.Center) {
                    Text("Se requieren permisos de sensores", color = Color.Gray)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState)
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sincronización Salud",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )

                    HealthCard(
                        title = "Ritmo Cardíaco",
                        value = healthData[WearConstants.KEY_BPM]?.toString() ?: "--",
                        unit = " BPM",
                        icon = "❤️"
                    )

                    HealthCard(
                        title = "Presión Atmosférica",
                        value = String.format("%.2f", healthData[WearConstants.KEY_PRESSURE] ?: 0.0),
                        unit = " kPa",
                        icon = "🌡️"
                    )

                    HealthCard(
                        title = "Temperatura Ambiente",
                        value = String.format("%.1f", healthData[WearConstants.KEY_TEMP] ?: 0.0),
                        unit = " °C",
                        icon = "🔥"
                    )

                    HealthCard(
                        title = "Humedad Relativa",
                        value = String.format("%.1f", healthData[WearConstants.KEY_HUMIDITY] ?: 0.0),
                        unit = " %",
                        icon = "💧"
                    )

                    HealthCard(
                        title = "Luz Ambiente",
                        value = String.format("%.0f", healthData[WearConstants.KEY_LIGHT] ?: 0.0),
                        unit = " lux",
                        icon = "☀️"
                    )

                    HealthCard(
                        title = "Oxígeno en Sangre",
                        value = healthData[WearConstants.KEY_SPO2]?.toString() ?: "--",
                        unit = " %",
                        icon = "🩸"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.syncFromWatch() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(ButtonGradient, shape = RoundedCornerShape(28.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Sincronizar desde Reloj", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.uploadToCloud() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Subir a MongoDB Atlas", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun HealthCard(title: String, value: String, unit: String, icon: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, color = Color.Gray, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = unit,
                        color = BlueGradient,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                }
            }
            Text(text = icon, fontSize = 34.sp)
        }
    }
}
