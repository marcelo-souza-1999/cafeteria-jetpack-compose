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
    val logoSplash: Dp = 200.dp,
    val logoAuth: Dp = 120.dp,
    val alphaOverlay: Float = 0.7f,
    val zIndexOverlay: Float = 10f,
    val cardImageHeight: Dp = 110.dp,
    val iconSizeSmall: Dp = 16.dp,
    val iconSizeMedium: Dp = 24.dp,
    val iconSizeLarge: Dp = 40.dp,
    val buttonHeightMedium: Dp = 36.dp,
    val descriptionHeight: Dp = 32.dp,
    val bottomSheetImageHeight: Dp = 180.dp,
    val iconSizeExtraLarge: Dp = 80.dp,
    val quantitySelectorWidth: Dp = 130.dp,
    val quantitySelectorHeight: Dp = 44.dp,
    // Profile Screen Tokens
    val avatarLarge: Dp = 130.dp,
    val avatarPreset: Dp = 45.dp,
    val borderThick: Dp = 3.dp,
    val avatarSelectorIcon: Dp = 36.dp,
    // Checkout Screen Tokens
    val buttonHeightLarge: Dp = 56.dp,
    val stateFieldWidth: Dp = 80.dp,
    // Chat Screen Tokens
    val chatAvatarSize: Dp = 28.dp,
    val chatInputRadius: Dp = 24.dp,
    val chatBubbleRadius: Dp = 16.dp,
    val chatBubbleMaxWidth: Dp = 280.dp,
    val chatThinkingRadius: Dp = 12.dp,
    val chatSendButtonSize: Dp = 48.dp,
)

val LocalTargaryenDimens = staticCompositionLocalOf { TargaryenDimens() }
