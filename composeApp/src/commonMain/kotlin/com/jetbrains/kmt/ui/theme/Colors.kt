package com.jetbrains.kmt.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class UiColors(
    val primary: Color,
    val secondary: Color,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val muted: Color,
    val success: Color,
    val error: Color,
    val errorBackground: Color,
    val errorText: Color,
    val editorBackground: Color,
    val editorBorder: Color,
    val lineNumber: Color,
    val tokenKeyword: Color,
    val tokenNumber: Color,
    val tokenString: Color,
    val tokenOperator: Color,
    val tokenIdentifier: Color,
)

val LightUiColors =
    UiColors(
        primary = Color(0xFF0B7285),
        secondary = Color(0xFFB08968),
        backgroundTop = Color(0xFFF8F5F2),
        backgroundBottom = Color(0xFFF1EFEA),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1B1A19),
        onSurfaceMuted = Color(0xFF1F2D3D),
        muted = Color(0xFF6B7280),
        success = Color(0xFF2F855A),
        error = Color(0xFFD64545),
        errorBackground = Color(0xFFFFF1F1),
        errorText = Color(0xFF7C2D2D),
        editorBackground = Color(0xFFFCFBF9),
        editorBorder = Color(0xFFE4DED6),
        lineNumber = Color(0xFF9CA3AF),
        tokenKeyword = Color(0xFF0B7285),
        tokenNumber = Color(0xFFAD5D0B),
        tokenString = Color(0xFF2F855A),
        tokenOperator = Color(0xFF6B7280),
        tokenIdentifier = Color(0xFF1F2D3D),
    )

val DarkUiColors =
    UiColors(
        primary = Color(0xFF4DB6C6),
        secondary = Color(0xFFC49A6C),
        backgroundTop = Color(0xFF121414),
        backgroundBottom = Color(0xFF1A1D1E),
        surface = Color(0xFF1F2326),
        onSurface = Color(0xFFEDEDED),
        onSurfaceMuted = Color(0xFFC9D1D9),
        muted = Color(0xFF9AA0A6),
        success = Color(0xFF49C27C),
        error = Color(0xFFE57373),
        errorBackground = Color(0xFF3A1E1E),
        errorText = Color(0xFFF2B8B5),
        editorBackground = Color(0xFF1C1F22),
        editorBorder = Color(0xFF2B2F33),
        lineNumber = Color(0xFF6B7280),
        tokenKeyword = Color(0xFF4DB6C6),
        tokenNumber = Color(0xFFE3B341),
        tokenString = Color(0xFF7EE787),
        tokenOperator = Color(0xFF9AA0A6),
        tokenIdentifier = Color(0xFFE6EDF3),
    )

val LocalUiColors = staticCompositionLocalOf { LightUiColors }
