package com.example.personamessenger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleIosColorScheme = lightColorScheme(
    primary = AppleGreen,
    onPrimary = PureWhite,
    primaryContainer = AppleGreenLight,
    onPrimaryContainer = AppleGreenDark,
    secondary = LightPurple,
    onSecondary = PureWhite,
    secondaryContainer = LightPurpleBackground,
    onSecondaryContainer = LightPurpleText,
    tertiary = LightPurpleSoft,
    onTertiary = PureWhite,
    background = AppleBackground,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = AppleBackground,
    onSurfaceVariant = TextSecondary,
    outline = AppleBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppleIosColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
