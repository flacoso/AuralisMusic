package com.zapia.auralis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkAuralisColors = darkColorScheme(
    primary = Color(0xFFB8FF5A), onPrimary = Color(0xFF142000), secondary = Color(0xFF8BE9FD),
    background = Color(0xFF0B0D10), surface = Color(0xFF15191E), surfaceVariant = Color(0xFF20262D),
    onBackground = Color(0xFFF4F7F2), onSurface = Color(0xFFF4F7F2), onSurfaceVariant = Color(0xFFAEB7B2)
)
private val LightAuralisColors = lightColorScheme(
    primary = Color(0xFF3D6E00), onPrimary = Color.White, secondary = Color(0xFF006779),
    background = Color(0xFFF7FAF3), surface = Color.White, surfaceVariant = Color(0xFFE5E9E0),
    onBackground = Color(0xFF171A15), onSurface = Color(0xFF171A15), onSurfaceVariant = Color(0xFF5F665B)
)

@Composable
fun AuralisTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkAuralisColors else LightAuralisColors, content = content)
}
