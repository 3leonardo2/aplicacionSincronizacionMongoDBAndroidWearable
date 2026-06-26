package com.example.SincronizacionApp.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

val PurpleGradient = Color(0xFF8E24AA)
val BlueGradient = Color(0xFF1E88E5)

val ButtonGradient = Brush.linearGradient(
    colors = listOf(PurpleGradient, BlueGradient)
)

@Composable
fun SincronizacionAppTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        content = content
    )
}