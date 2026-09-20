package com.wishify.customer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = SurfaceLight,
    primaryContainer = RoseLight,
    onPrimaryContainer = PlumDark,
    secondary = PlumText,
    onSecondary = SurfaceLight,
    tertiary = GoldAccent,
    onTertiary = PlumDark,
    background = BlushBackground,
    onBackground = PlumText,
    surface = SurfaceLight,
    onSurface = PlumText,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = PlumText
)

private val DarkColorScheme = darkColorScheme(
    primary = RosePrimaryDark,
    onPrimary = PlumDark,
    primaryContainer = RoseDark,
    onPrimaryContainer = TextLight,
    secondary = GoldAccent,
    onSecondary = PlumDark,
    tertiary = GoldDark,
    onTertiary = TextLight,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextMuted
)

@Composable
fun WishifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
