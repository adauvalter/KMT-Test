package com.jetbrains.kmt

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jetbrains.kmt.generated.resources.Res
import com.jetbrains.kmt.generated.resources.kmt
import org.jetbrains.compose.resources.getDrawableResourceBytes
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import org.jetbrains.compose.resources.painterResource
import java.awt.Taskbar
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMTTestTask",
            icon = painterResource(Res.drawable.kmt),
            state =
                rememberWindowState(
                    width = WINDOW_START_WIDTH.dp,
                    height = WINDOW_START_HEIGHT.dp,
                ),
        ) {
            LaunchedEffect(Unit) {
                setTaskbarIcon()
            }
            App()
        }
    }

private suspend fun setTaskbarIcon() {
    if (!Taskbar.isTaskbarSupported()) return
    val taskbar = Taskbar.getTaskbar()
    if (!taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) return
    taskbar.iconImage = loadKmtIconImage() ?: return
}

private suspend fun loadKmtIconImage(): BufferedImage? {
    return try {
        val environment = getSystemResourceEnvironment()
        val bytes = getDrawableResourceBytes(environment, Res.drawable.kmt)
        ImageIO.read(ByteArrayInputStream(bytes))
    } catch (_: Exception) {
        null
    }
}

private const val WINDOW_START_WIDTH = 1200
private const val WINDOW_START_HEIGHT = 800
