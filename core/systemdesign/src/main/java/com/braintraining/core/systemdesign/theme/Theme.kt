package com.braintraining.core.systemdesign.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorTheme = lightColorScheme(
    primary = Blue80,
    onPrimary = White,
    secondary = Brown20,
    onSecondary = White,
    tertiary = Blue20,
    onTertiary = White,
    background = Beige80,
    onBackground = Black,
    surface = PinkChampagne80,
    onSurface = Black
)

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorTheme,
        typography = AppType,
        shapes = AppShapes,
        content = content
    )
}