package com.tiltsensor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2D2D2D)

val GreenAngle = Color(0xFF4CAF50)
val YellowAngle = Color(0xFFFFEB3B)
val RedAngle = Color(0xFFF44336)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)

private val DarkColorScheme = darkColorScheme(
    primary = GreenAngle,
    secondary = YellowAngle,
    tertiary = RedAngle,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun TiltSensorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

fun getAngleColor(angle: Float): Color {
    val absAngle = kotlin.math.abs(angle)
    return when {
        absAngle < 30f -> GreenAngle
        absAngle < 50f -> YellowAngle
        else -> RedAngle
    }
}
