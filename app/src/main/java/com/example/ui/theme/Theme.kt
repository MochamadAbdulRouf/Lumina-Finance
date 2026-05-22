package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrandLime,
    onPrimary = Color(0xFF381E72),
    primaryContainer = BrandLimeLight,
    onPrimaryContainer = Color(0xFF21005D),
    secondary = TextLightCold,
    onSecondary = BrandSurfaceNavy,
    background = BrandNavyBlack,
    onBackground = TextLightCold,
    surface = BrandSurfaceNavy,
    onSurface = TextLightCold,
    surfaceVariant = Color(0xFF332D41),
    onSurfaceVariant = TextGrayMuted,
    outline = BrandBorderSlate,
    outlineVariant = Color(0xFF49454F),
    error = BrandErrorRed,
    onError = Color(0xFF601410)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark-Mode first design alignment
    dynamicColor: Boolean = false, // Maintain premium brand styling rather than android customization overrides
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
