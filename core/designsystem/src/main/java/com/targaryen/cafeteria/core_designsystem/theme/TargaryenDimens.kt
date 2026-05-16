package com.targaryen.cafeteria.core_designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TargaryenDimens(
    val none: Dp = 0.dp,
    val spaceExtraSmall: Dp = 4.dp,
    val spaceSmall: Dp = 8.dp,
    val spaceMedium: Dp = 10.dp,
    val spaceNormal: Dp = 16.dp,
    val spaceLarge: Dp = 24.dp,
    val spaceExtraLarge: Dp = 32.dp,
    val spaceMassive: Dp = 48.dp,

    val radiusSmall: Dp = 4.dp,
    val radiusMedium: Dp = 8.dp,
    val radiusLarge: Dp = 16.dp,

    val borderSmall: Dp = 1.dp,
    val borderMedium: Dp = 2.dp,
    
    val buttonContentHorizontal: Dp = 12.dp,
    val buttonContentVertical: Dp = 12.dp,

    val logoSplash: Dp = 200.dp
)

val LocalTargaryenDimens = staticCompositionLocalOf { TargaryenDimens() }
