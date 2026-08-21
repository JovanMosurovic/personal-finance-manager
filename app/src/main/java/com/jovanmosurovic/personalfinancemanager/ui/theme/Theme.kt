package com.jovanmosurovic.personalfinancemanager.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = FinanceBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF19375B),
    onPrimaryContainer = Color(0xFFD6E8FF),
    secondary = FinanceGreenDark,
    onSecondary = Color(0xFF001B08),
    secondaryContainer = Color(0xFF174C2A),
    onSecondaryContainer = Color(0xFFB5F0C6),
    tertiary = FinanceCoralDark,
    onTertiary = Color(0xFF2B0503),
    tertiaryContainer = Color(0xFF5A2420),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = FinanceBackgroundDark,
    onBackground = FinanceInkDark,
    surface = Color(0xFF1C1C1E),
    onSurface = FinanceInkDark,
    surfaceVariant = FinanceSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC1C9D9),
    outline = FinanceOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = FinanceBlue,
    onPrimary = Color.White,
    primaryContainer = FinanceBlueLight,
    onPrimaryContainer = Color(0xFF00164F),
    secondary = FinanceGreen,
    onSecondary = Color.White,
    secondaryContainer = FinanceGreenLight,
    onSecondaryContainer = Color(0xFF002116),
    tertiary = FinanceCoral,
    onTertiary = Color.White,
    tertiaryContainer = FinanceCoralLight,
    onTertiaryContainer = Color(0xFF410003),
    background = FinanceBackground,
    onBackground = FinanceInk,
    surface = Color.White,
    onSurface = FinanceInk,
    surfaceVariant = FinanceSurfaceVariant,
    onSurfaceVariant = Color(0xFF5C6475),
    outline = FinanceOutline
)

private val FinanceShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun PersonalfinancemanagerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = FinanceShapes,
        content = content
    )
}
