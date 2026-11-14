package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val UpzLightColorScheme = lightColorScheme(
    primary = EmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = EmeraldMintContainer,
    onPrimaryContainer = EmeraldOnMintContainer,
    secondary = GoldPrimary,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldOnContainer,
    tertiary = TealAccent,
    onTertiary = Color.White,
    tertiaryContainer = TealContainer,
    onTertiaryContainer = TealOnContainer,
    background = IvoryBackground,
    onBackground = InkDark,
    surface = PureSurface,
    onSurface = InkDark,
    surfaceVariant = SoftSageSurface,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = PureSurface,
    surfaceContainerLow = IvoryBackground,
    surfaceContainer = SoftSageSurface,
    surfaceContainerHigh = SoftSageContainerHigh,
    outline = OutlineSoft,
    outlineVariant = OutlineSubtle,
    error = StatusError,
    onError = Color.White,
    errorContainer = StatusErrorContainer,
)

private val UpzDarkColorScheme = darkColorScheme(
    primary = EmeraldLightPrimary,
    onPrimary = EmeraldDarkOnPrimary,
    primaryContainer = EmeraldDarkContainer,
    onPrimaryContainer = EmeraldMintContainer,
    secondary = GoldLightPrimary,
    onSecondary = GoldOnContainer,
    secondaryContainer = GoldDarkContainer,
    onSecondaryContainer = GoldContainer,
    tertiary = TealContainer,
    onTertiary = TealOnContainer,
    background = ForestBackgroundDark,
    onBackground = InkLight,
    surface = ForestSurfaceDark,
    onSurface = InkLight,
    surfaceVariant = ForestSurfaceContainerDark,
    onSurfaceVariant = InkMutedDark,
    surfaceContainerLowest = ForestBackgroundDark,
    surfaceContainerLow = ForestSurfaceDark,
    surfaceContainer = ForestSurfaceContainerDark,
    surfaceContainerHigh = ForestSurfaceContainerHighDark,
    outline = EmeraldBright,
    outlineVariant = ForestOutlineSubtleDark,
    error = StatusError,
    errorContainer = StatusErrorContainer,
)

val UpzShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun UpzCikulakTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> UpzDarkColorScheme
        else -> UpzLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UpzTypography,
        shapes = UpzShapes,
        content = content,
    )
}
