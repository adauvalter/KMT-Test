package com.jetbrains.kmt.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.jetbrains.kmt.lang.api.AnalysisResult
import com.jetbrains.kmt.lang.api.Analyzer
import com.jetbrains.kmt.lang.diagnostics.Diagnostic
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.ui.theme.Dimens
import com.jetbrains.kmt.ui.theme.LocalUiColors
import com.jetbrains.kmt.ui.theme.UiColors
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@Composable
fun EditorScreen() {
    var textValue by remember { mutableStateOf(TextFieldValue(sampleProgram)) }
    var diagnostics by remember { mutableStateOf<List<Diagnostic>>(emptyList()) }
    var output by remember { mutableStateOf("") }
    var showIndicator by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val colors = LocalUiColors.current
    LaunchedEffect(Unit) {
        var latestRequest: String
        snapshotFlow { textValue.text }
            .debounce(250)
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                latestRequest = snapshot
                diagnostics = emptyList()
                output = ""
                showIndicator = false
                val indicatorJob =
                    launch {
                        delay(500)
                        showIndicator = true
                    }
                try {
                    val result: AnalysisResult = Analyzer.analyze(snapshot)
                    if (latestRequest != snapshot) return@collectLatest
                    diagnostics = result.diagnostics
                    output = if (diagnostics.isEmpty()) result.output else ""
                } finally {
                    indicatorJob.cancel()
                    showIndicator = false
                }
            }
    }

    val background =
        Brush.linearGradient(
            colors = listOf(colors.backgroundTop, colors.backgroundBottom),
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(background)
                .padding(Dimens.ScreenPadding),
    ) {
        Header(colors = colors)
        Spacer(Modifier.height(Dimens.PanelSpacing))
        Row(modifier = Modifier.weight(1f)) {
            Panel(
                title = "Editor",
                colors = colors,
                modifier = Modifier.weight(0.6f).fillMaxSize(),
            ) {
                CodeEditor(
                    value = textValue,
                    diagnostics = diagnostics,
                    focusRequester = focusRequester,
                    onValueChange = { textValue = it },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(Dimens.PanelSpacing))
            Column(
                modifier = Modifier.weight(0.4f).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.PanelSpacing),
            ) {
                Panel(title = "Output", colors = colors, modifier = Modifier.weight(0.55f)) {
                    val outputStyle =
                        TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = Dimens.OutputFontSize,
                            color = colors.onSurfaceMuted,
                        )
                    val scroll = rememberScrollState()
                    Text(
                        text = output.ifBlank { "No output yet." },
                        style = outputStyle,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(scroll),
                    )
                }
                Panel(title = "Diagnostics", colors = colors, modifier = Modifier.weight(0.45f)) {
                    DiagnosticsList(
                        diagnostics = diagnostics,
                        colors = colors,
                        onDiagnosticClick = { diagnostic ->
                            val offset = diagnostic.span.startOffset.coerceIn(0, textValue.text.length)
                            textValue = textValue.copy(selection = TextRange(offset))
                            focusRequester.requestFocus()
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(Dimens.PanelSpacing))
        StatusIndicator(
            showIndicator = showIndicator,
            diagnostics = diagnostics,
            colors = colors,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun Header(colors: UiColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "KMT Editor",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Live parsing, type-checking, and evaluation",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
            )
        }
    }
}

@Composable
private fun Panel(
    title: String,
    colors: UiColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.PanelElevation),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Dimens.PanelPadding),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = colors.muted,
            )
            Spacer(Modifier.height(Dimens.PanelTitleSpacing))
            content()
        }
    }
}

@Composable
private fun DiagnosticsList(
    diagnostics: List<Diagnostic>,
    colors: UiColors,
    onDiagnosticClick: (Diagnostic) -> Unit,
) {
    val scroll = rememberScrollState()
    val textStyle =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = Dimens.DiagnosticFontSize,
            color = colors.onSurfaceMuted,
        )
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(Dimens.DiagnosticsSpacing),
    ) {
        AnimatedVisibility(visible = diagnostics.isEmpty()) {
            Text("No issues detected.", style = textStyle, color = colors.success)
        }
        for (diag in diagnostics) {
            val line = diag.span.line
            val col = diag.span.column
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onDiagnosticClick(diag) },
                colors = CardDefaults.cardColors(containerColor = colors.errorBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Zero),
            ) {
                Column(modifier = Modifier.padding(Dimens.DiagnosticsCardPadding)) {
                    Text(
                        text = "Line $line:$col",
                        style = textStyle.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = diag.message,
                        style = textStyle,
                        color = colors.errorText,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    showIndicator: Boolean,
    diagnostics: List<Diagnostic>,
    colors: UiColors,
    modifier: Modifier = Modifier,
) {
    val showIssues = diagnostics.isNotEmpty()
    val showContent = showIndicator || showIssues
    Box(
        modifier =
            modifier
                .width(Dimens.StatusPillWidth)
                .height(Dimens.StatusPillHeight),
        contentAlignment = Alignment.Center,
    ) {
        if (showContent) {
            val statusText = if (showIssues) "${diagnostics.size} issue(s)" else ""
            val statusColor = if (showIssues) MaterialTheme.colorScheme.error else colors.primary
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(Dimens.StatusPillPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showIndicator) {
                        LinearProgressIndicator(
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.2f),
                            modifier =
                                Modifier
                                    .width(Dimens.StatusIndicatorWidth)
                                    .height(Dimens.StatusIndicatorHeight),
                        )
                    } else {
                        Text(
                            text = statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

private val sampleProgram =
    """
var n = 500
var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))
var pi = 4 * reduce(sequence, 0, x y -> x + y)
print "pi = "
out pi
    """.trimIndent()

@Preview
@Composable
private fun EditorScreenPreview() {
    AppTheme {
        EditorScreen()
    }
}

@Preview
@Composable
private fun DiagnosticsPreview() {
    val diagnostics =
        listOf(
            Diagnostic("Sequence bounds must be integers", SourceSpan(12, 3, 1, 13)),
            Diagnostic("Undefined variable 'pi'", SourceSpan(54, 2, 4, 5)),
        )
    AppTheme {
        val colors = LocalUiColors.current
        Panel(title = "Diagnostics", colors = colors) {
            DiagnosticsList(diagnostics = diagnostics, colors = colors, onDiagnosticClick = {})
        }
    }
}
