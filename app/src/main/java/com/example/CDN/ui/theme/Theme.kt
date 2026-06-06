package com.example.CDN.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberRed,
    secondary = CyberNeonRed,
    tertiary = CyberYellow,
    background = CyberBlack, // Pitch black for OLED screen battery saving
    surface = CyberDarkGray,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = CyberBlack,
    onBackground = CyberWhite,
    onSurface = CyberWhite,
    surfaceVariant = CyberMediumGray,
    onSurfaceVariant = CyberMutedText,
    outline = CyberMutedRed
)

private val CyberLightColorScheme = lightColorScheme(
    primary = CyberRed,
    secondary = CyberMutedRed,
    tertiary = CyberYellow,
    background = CyberLightBg,
    surface = CyberLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = CyberLightText,
    onBackground = CyberLightText,
    onSurface = CyberLightText,
    surfaceVariant = CyberLightGray,
    onSurfaceVariant = CyberLightText,
    outline = CyberMutedRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
