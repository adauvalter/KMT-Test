package com.jetbrains.kmt.ui

internal data class LineInfo(
    val number: Int?,
    val topPx: Float,
    val heightPx: Float,
)

internal data class LineMetrics(
    val topPx: Float,
    val heightPx: Float,
)

internal fun computeLineStarts(text: String): List<Int> {
    val lineStarts = ArrayList<Int>()
    lineStarts.add(0)
    for (index in text.indices) {
        val ch = text[index]
        val isLineBreak =
            ch == '\n' || (ch == '\r' && text.getOrNull(index + 1) != '\n')
        if (isLineBreak) {
            lineStarts.add(index + 1)
        }
    }
    return lineStarts
}

internal fun lineInfosWithoutLayout(
    lineStarts: List<Int>,
    fallbackLineHeightPx: Float,
): List<LineInfo> =
    List(lineStarts.size) { index ->
        LineInfo(index + 1, index * fallbackLineHeightPx, fallbackLineHeightPx)
    }

internal fun assignLineNumbers(
    lineStarts: List<Int>,
    visualLines: List<LineMetrics>,
    offsetToLineIndex: (Int) -> Int,
    fallbackLineHeightPx: Float,
): List<LineInfo> {
    if (visualLines.isEmpty()) {
        return lineInfosWithoutLayout(lineStarts, fallbackLineHeightPx)
    }
    val result = ArrayList<LineInfo>(visualLines.size)
    for (line in visualLines) {
        result.add(LineInfo(null, line.topPx, line.heightPx))
    }
    val missingNumbers = ArrayList<Int>()
    for (index in lineStarts.indices) {
        val number = index + 1
        val offset = lineStarts[index]
        val lineIndex = offsetToLineIndex(offset)
        if (lineIndex in result.indices && result[lineIndex].number == null) {
            val info = result[lineIndex]
            result[lineIndex] = info.copy(number = number)
        } else {
            missingNumbers.add(number)
        }
    }
    if (missingNumbers.isNotEmpty()) {
        var top = result.last().topPx + result.last().heightPx
        for (number in missingNumbers) {
            result.add(LineInfo(number, top, fallbackLineHeightPx))
            top += fallbackLineHeightPx
        }
    }
    return result
}
