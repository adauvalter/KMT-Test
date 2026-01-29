package com.jetbrains.kmt.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jetbrains.kmt.ui.theme.DarkUiColors
import com.jetbrains.kmt.ui.theme.LightUiColors
import com.jetbrains.kmt.ui.theme.LocalUiColors
import com.jetbrains.kmt.ui.theme.UiDimens

private val LightAppColors: ColorScheme =
    lightColorScheme(
        primary = LightUiColors.primary,
        onPrimary = LightUiColors.surface,
        secondary = LightUiColors.secondary,
        onSecondary = LightUiColors.onSurface,
        background = LightUiColors.backgroundTop,
        onBackground = LightUiColors.onSurface,
        surface = LightUiColors.surface,
        onSurface = LightUiColors.onSurface,
        error = LightUiColors.error,
        onError = LightUiColors.surface,
    )

private val DarkAppColors: ColorScheme =
    darkColorScheme(
        primary = DarkUiColors.primary,
        onPrimary = DarkUiColors.surface,
        secondary = DarkUiColors.secondary,
        onSecondary = DarkUiColors.onSurface,
        background = DarkUiColors.backgroundTop,
        onBackground = DarkUiColors.onSurface,
        surface = DarkUiColors.surface,
        onSurface = DarkUiColors.onSurface,
        error = DarkUiColors.error,
        onError = DarkUiColors.surface,
    )

private val AppTypography =
    Typography(
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = UiDimens.TitleFontSize,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = UiDimens.BodyLargeFontSize,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = UiDimens.BodyMediumFontSize,
            ),
        labelMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = UiDimens.LabelMediumFontSize,
            ),
    )

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkUiColors else LightUiColors
    val colorScheme = if (darkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(LocalUiColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
