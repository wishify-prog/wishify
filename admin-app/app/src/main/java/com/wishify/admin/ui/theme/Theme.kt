package com.wishify.admin.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = DarkPlumBackground,
    primaryContainer = DarkPlumPrimary,
    onPrimaryContainer = TextPrimary,
    secondary = GoldDark,
    onSecondary = DarkPlumBackground,
    background = DarkPlumBackground,
    onBackground = TextPrimary,
    surface = DarkPlumSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkPlumSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun WishifyAdminTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkPlumBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        shapes = Shapes,
        content = content
    )
}
