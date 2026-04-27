package com.minimalledger.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val appColorScheme = lightColorScheme(
    primary = LedgerPrimary,
    secondary = LedgerAccent,
    background = LedgerBackground,
    surface = LedgerSurface,
    onPrimary = LedgerSurface,
    onSecondary = LedgerPrimary,
    onBackground = LedgerPrimary,
    onSurface = LedgerPrimary,
)

@Composable
fun MinimalLedgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = appColorScheme,
        typography = AppTypography,
        content = content,
    )
}
