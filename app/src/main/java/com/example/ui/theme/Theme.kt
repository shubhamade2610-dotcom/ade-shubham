package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NaturalPrimaryDark,
    secondary = NaturalSecondaryDark,
    tertiary = NaturalTertiaryDark,
    background = NaturalBgDark,
    surface = Color(0xFF2C1613),
    onPrimary = Color(0xFF410001),
    onSecondary = Color(0xFF2C1512),
    onTertiary = Color(0xFF2C1512),
    onBackground = NaturalTextDark,
    onSurface = NaturalTextDark,
    primaryContainer = Color(0xFF5D3F3C),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondaryContainer = Color(0xFF4E2A28),
    onSecondaryContainer = Color(0xFFE7A195),
    outlineVariant = Color(0xFF4E3734)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = NaturalPrimaryLight,
    secondary = NaturalSecondaryLight,
    tertiary = NaturalTertiaryLight,
    background = NaturalBgLight,
    surface = Color(0xFFFFF7F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = NaturalTextLight,
    onSurface = NaturalTextLight,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410001),
    secondaryContainer = Color(0xFFF9DEDC),
    onSecondaryContainer = Color(0xFFA35D50),
    outlineVariant = Color(0xFFE7D6D3)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to preserve the brand's soulful aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
