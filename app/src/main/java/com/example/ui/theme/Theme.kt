package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BallMasterColorScheme = lightColorScheme(
    primary = GamePalette.MainWoodenBrown,
    onPrimary = GamePalette.TextWhite,
    primaryContainer = GamePalette.OrangeWood,
    onPrimaryContainer = GamePalette.DarkWoodOutline,
    secondary = GamePalette.JungleGreen,
    onSecondary = GamePalette.TextWhite,
    secondaryContainer = GamePalette.LeafGreen,
    onSecondaryContainer = GamePalette.DeepForestShadow,
    tertiary = GamePalette.BrightGold,
    onTertiary = GamePalette.DarkWoodOutline,
    background = GamePalette.SkyBlue,
    onBackground = GamePalette.TextWhite,
    surface = GamePalette.DeepWoodBrown,
    onSurface = GamePalette.TextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BallMasterColorScheme,
        typography = Typography,
        content = content
    )
}
