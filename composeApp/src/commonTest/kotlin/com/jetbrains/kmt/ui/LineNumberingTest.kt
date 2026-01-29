package com.jetbrains.kmt.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LineNumberingTest {
    @Test
    fun computeLineStartsHandlesMixedLineEndings() {
        val text = "a\nb\r\nc\rd"
        assertEquals(listOf(0, 2, 5, 7), computeLineStarts(text))
    }

    @Test
    fun assignLineNumbersMarksFirstVisualLineOfEachLogicalLine() {
        val lineStarts = listOf(0, 4, 8)
        val visualLines =
            listOf(
                LineMetrics(0f, 10f),
                LineMetrics(10f, 10f),
                LineMetrics(20f, 10f),
                LineMetrics(30f, 10f),
                LineMetrics(40f, 10f),
            )
        val mapping = mapOf(0 to 0, 4 to 2, 8 to 4)
        val lineInfos =
            assignLineNumbers(
                lineStarts = lineStarts,
                visualLines = visualLines,
                offsetToLineIndex = { offset -> mapping.getValue(offset) },
                fallbackLineHeightPx = 10f,
            )
        assertEquals(listOf(1, null, 2, null, 3), lineInfos.map { it.number })
    }

    @Test
    fun assignLineNumbersAppendsMissingLogicalLines() {
        val lineStarts = listOf(0, 2, 4)
        val visualLines =
            listOf(
                LineMetrics(0f, 10f),
                LineMetrics(10f, 10f),
            )
        val mapping = mapOf(0 to 0, 2 to 1, 4 to 1)
        val lineInfos =
            assignLineNumbers(
                lineStarts = lineStarts,
                visualLines = visualLines,
                offsetToLineIndex = { offset -> mapping.getValue(offset) },
                fallbackLineHeightPx = 10f,
            )
        assertEquals(listOf(1, 2, 3), lineInfos.map { it.number })
        assertEquals(listOf(0f, 10f, 20f), lineInfos.map { it.topPx })
    }
}
