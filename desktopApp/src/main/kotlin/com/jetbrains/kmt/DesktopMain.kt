package com.jetbrains.kmt

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.Taskbar
import java.awt.image.BufferedImage

fun main() =
    application {
        setTaskbarIcon()
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMTTestTask",
            state =
                rememberWindowState(
                    width = WINDOW_START_WIDTH.dp,
                    height = WINDOW_START_HEIGHT.dp,
                ),
        ) {
            App()
        }
    }

private fun setTaskbarIcon() {
    if (!Taskbar.isTaskbarSupported()) return
    val taskbar = Taskbar.getTaskbar()
    if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return
    taskbar.iconImage = createKmtIconImage()
}

private fun createKmtIconImage(): BufferedImage {
    val image = BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.createGraphics()
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
    )
    graphics.color = Color(0x1F, 0x2D, 0x3D)
    graphics.fillRoundRect(0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE / 5, ICON_SIZE / 5)
    val text = "KMT"
    val paddingX = (ICON_SIZE * 0.12f).toInt()
    val maxTextWidth = ICON_SIZE - (paddingX * 2)
    var fontSize = (ICON_SIZE * 0.46f).toInt()
    graphics.font = Font("SansSerif", Font.BOLD, fontSize)
    var metrics = graphics.fontMetrics
    while (metrics.stringWidth(text) > maxTextWidth && fontSize > 1) {
        fontSize -= 1
        graphics.font = Font("SansSerif", Font.BOLD, fontSize)
        metrics = graphics.fontMetrics
    }
    val textWidth = metrics.stringWidth(text)
    val textHeight = metrics.ascent + metrics.descent
    val x = ((ICON_SIZE - textWidth) / 2f).toInt()
    val y = ((ICON_SIZE - textHeight) / 2f + metrics.ascent).toInt()
    graphics.color = Color(0xF8, 0xF5, 0xF2)
    graphics.drawString(text, x, y)

    graphics.dispose()
    return image
}

private const val ICON_SIZE = 256
private const val WINDOW_START_WIDTH = 1200
private const val WINDOW_START_HEIGHT = 800
