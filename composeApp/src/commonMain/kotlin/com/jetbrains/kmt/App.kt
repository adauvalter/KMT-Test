package com.jetbrains.kmt

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jetbrains.kmt.ui.AppTheme
import com.jetbrains.kmt.ui.EditorScreen

@Composable
@Preview
fun App() {
    AppTheme {
        EditorScreen()
    }
}
