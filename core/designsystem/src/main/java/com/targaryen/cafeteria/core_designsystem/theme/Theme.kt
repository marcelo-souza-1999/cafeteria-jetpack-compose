package com.targaryen.cafeteria.core_designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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

    CompositionLocalProvider(
        LocalTargaryenDimens provides TargaryenDimens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object TargaryenTheme {
    val dimens: TargaryenDimens
        @Composable
        get() = LocalTargaryenDimens.current
}
