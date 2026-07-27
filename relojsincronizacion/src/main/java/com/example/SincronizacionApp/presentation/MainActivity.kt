package com.example.SincronizacionApp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.*
import com.example.SincronizacionApp.presentation.theme.*
import com.example.SincronizacionApp.utils.RequestHealthPermissions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info

import androidx.compose.ui.text.style.TextAlign

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
                .background(Color(0xFF000000)),
            contentAlignment = Alignment.Center
        ) {
            RequestHealthPermissions {
                permissionsGranted = true
            }
            if (!permissionsGranted) {
                Text("Esperando permisos...", color = Color.Gray, fontSize = 12.sp)
            } else {
                WatchPagerScreen(mainViewModel)
            }
        }
    }
}

@Composable
fun WatchPagerScreen(viewModel: MainViewModel) {
    val healthState by viewModel.healthState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 4 })
    
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> NutritionPage(
                    consumed = healthState.caloriesConsumed,
                    goal = healthState.caloriesGoal,
                    nextMealName = healthState.nextMealName,
                    nextMealTime = healthState.nextMealTime
                )
                1 -> SensorPage(
                    icon = Icons.Default.Favorite,
                    iconColor = Color(0xFFFF1744),
                    title = "RITMO CARDÍACO",
                    value = "${if(healthState.bpm > 0) healthState.bpm else "--"}",
                    unit = "BPM",
                    progress = if(healthState.bpm > 0) healthState.bpm / 200f else 0f
                )
                2 -> SensorPage(
                    icon = Icons.Default.DirectionsWalk,
                    iconColor = Color(0xFF00E5FF),
                    title = "PASOS HOY",
                    value = "${healthState.steps}",
                    unit = "pasos",
                    progress = healthState.steps / 10000f
                )
                3 -> SensorPage(
                    icon = Icons.Default.LocalFireDepartment,
                    iconColor = Color(0xFFFF9100),
                    title = "CALORÍAS QUEMADAS",
                    value = "${healthState.calories}",
                    unit = "kcal",
                    progress = healthState.calories / 500f
                )
            }
        }
        
        HorizontalPageIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            selectedColor = Color.White,
            unselectedColor = Color.Gray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun NutritionPage(
    consumed: Int,
    goal: Int,
    nextMealName: String,
    nextMealTime: String
) {
    val progress = consumed.toFloat() / goal.toFloat()
    val iconColor = Color(0xFF00E676)
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize().padding(8.dp),
            startAngle = 140f,
            endAngle = 40f,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = iconColor,
                trackColor = iconColor.copy(alpha = 0.1f)
            ),
            strokeWidth = 6.dp
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Botón de información movido a una zona segura (centrado arriba)
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "DIETA DIARIA",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$consumed",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "DE $goal KCAL",
                color = iconColor.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Etiqueta de próxima comida
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color.White.copy(alpha = 0.1f), shape = CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$nextMealName • $nextMealTime",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Diálogo de aviso con diseño centrado y botón redondo rosado
    AlertDialog(
        visible = showDialog,
        onDismissRequest = { showDialog = false },
        title = { 
            Text(
                "Sincronización", 
                textAlign = TextAlign.Center, 
                modifier = Modifier.fillMaxWidth(),
                fontSize = 15.sp 
            ) 
        },
        text = { 
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Actualiza los datos de dieta desde tu teléfono.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Botón redondo y rosado
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF06292)),
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Text("Ok", fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun SensorPage(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    unit: String,
    progress: Float
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize().padding(8.dp),
            startAngle = 140f,
            endAngle = 40f,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = iconColor,
                trackColor = iconColor.copy(alpha = 0.1f)
            ),
            strokeWidth = 6.dp
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(iconColor.copy(alpha = 0.2f), Color.Transparent)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = unit.uppercase(),
                color = iconColor.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
