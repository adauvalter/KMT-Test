package com.jetbrains.kmt.lang.diagnostics

/**
 * A half-open span in the source text, tracked by absolute offset and line/column.
 */
data class SourceSpan(
    val startOffset: Int,
    val length: Int,
    val line: Int,
    val column: Int,
) {
    val endOffset: Int get() = startOffset + length
}

/**
 * Builds a span that starts at [start] and ends at the end of [end].
 */
fun mergeSpan(
    start: SourceSpan,
    end: SourceSpan,
): SourceSpan {
    val startOffset = start.startOffset
    val endOffset = end.endOffset
    return SourceSpan(
        startOffset = startOffset,
        length = (endOffset - startOffset).coerceAtLeast(0),
        line = start.line,
        column = start.column,
    )
}
