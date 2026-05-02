package com.targaryen.cafeteria.core_designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TeamBlackColorScheme = darkColorScheme(
  primary = BloodRed,
  onPrimary = TargaryenWhite,
  primaryContainer = FireRed,
  onPrimaryContainer = TargaryenWhite,

  secondary = ValyrianGold,
  onSecondary = Obsidian,
  secondaryContainer = DimmedGold,
  onSecondaryContainer = Obsidian,

  background = Obsidian,
  onBackground = SilverHair,

  surface = CharcoalBlack,
  onSurface = SilverHair,
  surfaceVariant = DragonScale,
  onSurfaceVariant = SilverHair
)

@Composable
fun TargaryenTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = TeamBlackColorScheme
  val view = LocalView.current

  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.background.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}