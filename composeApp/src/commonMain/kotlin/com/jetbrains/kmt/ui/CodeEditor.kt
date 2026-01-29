package com.jetbrains.kmt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.isUnspecified
import com.jetbrains.kmt.lang.diagnostics.Diagnostic
import com.jetbrains.kmt.lang.syntax.Lexer
import com.jetbrains.kmt.lang.syntax.TokenType
import com.jetbrains.kmt.ui.theme.LocalUiColors
import com.jetbrains.kmt.ui.theme.UiColors
import com.jetbrains.kmt.ui.theme.UiDimens
import kotlin.math.roundToInt

@Composable
fun CodeEditor(
    value: TextFieldValue,
    diagnostics: List<Diagnostic>,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalUiColors.current
    val textStyle =
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = UiDimens.EditorFontSize,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = UiDimens.EditorLineHeight,
        )

    val density = LocalDensity.current
    val annotated =
        remember(value.text, diagnostics, colors) {
            highlightSource(value.text, diagnostics, colors)
        }

    val transformation =
        remember(annotated) {
            VisualTransformation { _ -> TransformedText(annotated, OffsetMapping.Identity) }
        }

    val scrollState = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val lineHeightPx =
        with(density) {
            val lineHeight =
                if (textStyle.lineHeight.isUnspecified) {
                    textStyle.fontSize
                } else {
                    textStyle.lineHeight
                }
            lineHeight.toPx()
        }
    val lineInfos =
        remember(value.text, layoutResult, lineHeightPx) {
            buildLineInfos(value.text, layoutResult, lineHeightPx)
        }
    LaunchedEffect(
        value.selection,
        layoutResult,
        scrollState.viewportSize,
        horizontalScroll.viewportSize,
    ) {
        val layout = layoutResult ?: return@LaunchedEffect
        val offset = value.selection.end.coerceIn(0, value.text.length)
        val rect = layout.getCursorRect(offset)
        val padding = with(density) { UiDimens.EditorGutterPadding.toPx() }

        val viewHeight = scrollState.viewportSize
        if (viewHeight > 0) {
            val top = scrollState.value.toFloat()
            val bottom = top + viewHeight
            val target =
                when {
                    rect.top < top + padding -> rect.top - padding
                    rect.bottom > bottom - padding -> rect.bottom - viewHeight + padding
                    else -> null
                }
            if (target != null) {
                scrollState.scrollTo(target.coerceAtLeast(0f).roundToInt())
            }
        }

        val viewWidth = horizontalScroll.viewportSize
        if (viewWidth > 0) {
            val left = horizontalScroll.value.toFloat()
            val right = left + viewWidth
            val target =
                when {
                    rect.left < left + padding -> rect.left - padding
                    rect.right > right - padding -> rect.right - viewWidth + padding
                    else -> null
                }
            if (target != null) {
                horizontalScroll.scrollTo(target.coerceAtLeast(0f).roundToInt())
            }
        }
    }

    Row(
        modifier =
            modifier
                .clip(MaterialTheme.shapes.medium)
                .background(colors.editorBackground)
                .border(UiDimens.BorderWidth, colors.editorBorder, MaterialTheme.shapes.medium)
                .padding(UiDimens.EditorPadding)
                .verticalScroll(scrollState),
    ) {
        LineNumbers(
            lineInfos = lineInfos,
            textStyle = textStyle,
            colors = colors,
            modifier = Modifier.padding(end = UiDimens.EditorGutterPadding),
        )
        Spacer(Modifier.width(UiDimens.EditorGutterGap))
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(horizontalScroll),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = textStyle,
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .wrapContentWidth(unbounded = true)
                        .focusRequester(focusRequester),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = transformation,
                maxLines = Int.MAX_VALUE,
                onTextLayout = { layoutResult = it },
                decorationBox = { innerField ->
                    Box(Modifier.fillMaxSize()) {
                        innerField()
                    }
                },
            )
        }
    }
}

@Composable
private fun LineNumbers(
    lineInfos: List<LineInfo>,
    textStyle: TextStyle,
    colors: UiColors,
    modifier: Modifier = Modifier,
) {
    Layout(
        content = {
            for (info in lineInfos) {
                Text(
                    text = info.number?.toString() ?: "",
                    style = textStyle.copy(color = colors.lineNumber),
                )
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val width = placeables.maxOfOrNull { it.width } ?: 0
        val heightPx =
            lineInfos.maxOfOrNull { (it.topPx + it.heightPx).roundToInt() }?.coerceAtLeast(0) ?: 0
        val layoutWidth = constraints.constrainWidth(width)
        val layoutHeight = constraints.constrainHeight(heightPx)
        layout(layoutWidth, layoutHeight) {
            lineInfos.forEachIndexed { index, info ->
                val placeable = placeables[index]
                val y = (info.topPx + (info.heightPx - placeable.height) / 2f).roundToInt()
                placeable.placeRelative(0, y.coerceAtLeast(0))
            }
        }
    }
}

private fun buildLineInfos(
    text: String,
    layoutResult: TextLayoutResult?,
    fallbackLineHeightPx: Float,
): List<LineInfo> {
    val lineStarts = computeLineStarts(text)
    if (layoutResult == null) {
        return lineInfosWithoutLayout(lineStarts, fallbackLineHeightPx)
    }
    val visualLines =
        List(layoutResult.lineCount) { line ->
            val top = layoutResult.getLineTop(line)
            val bottom = layoutResult.getLineBottom(line)
            val height = (bottom - top).let { if (it <= 0f) fallbackLineHeightPx else it }
            LineMetrics(top, height)
        }
    return assignLineNumbers(
        lineStarts = lineStarts,
        visualLines = visualLines,
        offsetToLineIndex = { offset ->
            layoutResult.getLineForOffset(offset.coerceIn(0, text.length))
        },
        fallbackLineHeightPx = fallbackLineHeightPx,
    )
}

private fun highlightSource(
    source: String,
    diagnostics: List<Diagnostic>,
    colors: UiColors,
): AnnotatedString {
    val base = AnnotatedString.Builder(source)
    val lex = Lexer(source).lex()

    val keywordStyle = SpanStyle(color = colors.tokenKeyword, fontWeight = FontWeight.SemiBold)
    val numberStyle = SpanStyle(color = colors.tokenNumber)
    val stringStyle = SpanStyle(color = colors.tokenString)
    val operatorStyle = SpanStyle(color = colors.tokenOperator)
    val identifierStyle = SpanStyle(color = colors.tokenIdentifier)

    for (token in lex.tokens) {
        if (token.type == TokenType.Eof) continue
        val style =
            when (token.type) {
                TokenType.Keyword -> keywordStyle
                TokenType.Number -> numberStyle
                TokenType.String -> stringStyle
                TokenType.Operator, TokenType.Arrow -> operatorStyle
                TokenType.Identifier -> identifierStyle
                else -> null
            }
        if (style != null) {
            base.addStyle(style, token.span.startOffset, token.span.endOffset)
        }
    }

    val errorStyle =
        SpanStyle(
            color = colors.error,
            textDecoration = TextDecoration.Underline,
        )
    for (diagnostic in diagnostics) {
        val start = diagnostic.span.startOffset.coerceIn(0, source.length)
        val end = diagnostic.span.endOffset.coerceIn(0, source.length)
        if (start < end) {
            base.addStyle(errorStyle, start, end)
        }
    }

    return base.toAnnotatedString()
}
