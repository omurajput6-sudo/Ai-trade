package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberCyan,
    secondary = EmeraldGreen,
    tertiary = CrimsonRed,
    background = DeepBackground,
    surface = SurfaceCard,
    surfaceVariant = SurfaceCardVariant,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCFD8DC)
  )

private val LightColorScheme = DarkColorScheme // Crypto-traders strictly prefer high-performance dark modes, let's keep it high-contrast Dark in both states

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark mode for optimal glow and readability of green/red trading charts
  dynamicColor: Boolean = false, // Disable dynamic colors, as trading signals require absolute Green/Red/Cyan high contrast control
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
