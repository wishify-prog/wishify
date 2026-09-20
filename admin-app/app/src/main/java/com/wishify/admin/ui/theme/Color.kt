package com.wishify.admin.ui.theme

import androidx.compose.ui.graphics.Color

// Wishify Admin Brand Palette: White & Light Green Combo
val AdminGreenPrimary = Color(0xFF2E7D32)     // Forest Green for primary actions & branding
val AdminLightGreen = Color(0xFF4CAF50)       // Fresh Light Green
val AdminGreenContainer = Color(0xFFE8F5E9)   // Soft Light Green container
val AdminGreenBorder = Color(0xFFC8E6C9)      // Border tint

val AdminWhiteBackground = Color(0xFFF7FAF7)  // Clean off-white background
val AdminSurfaceWhite = Color(0xFFFFFFFF)     // Pure white surface
val AdminSurfaceVariant = Color(0xFFE8F5E9)   // Light green surface tint

val AdminTextPrimary = Color(0xFF1B381E)      // Deep botanical green/charcoal
val AdminTextSecondary = Color(0xFF5A705D)    // Muted botanical green

// Compatibility aliases so all existing screen references match the new palette
val GoldAccent = AdminGreenPrimary
val GoldDark = Color(0xFF1B5E20)
val GoldLight = AdminLightGreen

val DarkPlumPrimary = AdminGreenPrimary
val DarkPlumBackground = AdminWhiteBackground
val DarkPlumSurface = AdminSurfaceWhite
val DarkPlumSurfaceVariant = AdminSurfaceVariant

val TextPrimary = AdminTextPrimary
val TextSecondary = AdminTextSecondary

val StatusPlaced = Color(0xFF1976D2)
val StatusConfirmed = Color(0xFF0097A7)
val StatusPrepared = Color(0xFFF57C00)
val StatusOut = Color(0xFF43A047)
val StatusDelivered = Color(0xFF2E7D32)
val StatusCancelled = Color(0xFFD32F2F)
