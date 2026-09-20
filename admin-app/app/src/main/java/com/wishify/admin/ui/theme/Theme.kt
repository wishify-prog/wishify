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

private val LightColorScheme = lightColorScheme(
    primary = AdminGreenPrimary,
    onPrimary = AdminSurfaceWhite,
    primaryContainer = AdminGreenContainer,
    onPrimaryContainer = AdminGreenPrimary,
    secondary = AdminLightGreen,
    onSecondary = AdminSurfaceWhite,
    background = AdminWhiteBackground,
    onBackground = AdminTextPrimary,
    surface = AdminSurfaceWhite,
    onSurface = AdminTextPrimary,
    surfaceVariant = AdminSurfaceVariant,
    onSurfaceVariant = AdminTextSecondary
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
            window.statusBarColor = AdminSurfaceWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        shapes = Shapes,
        content = content
    )
}
