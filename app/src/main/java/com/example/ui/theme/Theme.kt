package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuxuryDarkColorScheme =
  darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1B1607),
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = GoldLight,
    onSecondary = Color(0xFF261D02),
    secondaryContainer = Color(0xFF332912),
    onSecondaryContainer = Color(0xFFFFECC2),
    tertiary = GoldBright,
    onTertiary = Color(0xFF1F1600),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
  )

private val LuxuryLightColorScheme =
  lightColorScheme(
    primary = Color(0xFF8C6D0F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDF94),
    onPrimaryContainer = Color(0xFF2B1F00),
    secondary = Color(0xFF6B5D3F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4E0BB),
    onSecondaryContainer = Color(0xFF241A04),
    tertiary = Color(0xFF9E7715),
    onTertiary = Color.White,
    background = Color(0xFFFCF9F3),
    onBackground = Color(0xFF1F1B14),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1B14),
    surfaceVariant = Color(0xFFEBE4D8),
    onSurfaceVariant = Color(0xFF4C453A),
    outline = Color(0xFFCCC3B4),
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to rich dark gold theme for jewelry aesthetics
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) LuxuryDarkColorScheme else LuxuryLightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

