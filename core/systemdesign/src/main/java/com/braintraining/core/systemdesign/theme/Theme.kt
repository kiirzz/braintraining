package com.braintraining.core.systemdesign.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorTheme = lightColorScheme(
    primary = Brown30,
    secondary = Olive,
    background = Brown80,
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