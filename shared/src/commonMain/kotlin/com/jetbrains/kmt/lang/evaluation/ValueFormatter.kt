package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.semantics.NumberType

/**
 * Formats runtime values for display.
 */
class ValueFormatter {
    fun formatValue(value: Value): String =
        when (value) {
            is NumberValue -> formatNumber(value)
            is SequenceValue -> formatSequence(value)
        }

    private fun formatNumber(value: NumberValue): String =
        if (value.kind == NumberType.IntType) {
            value.asLong().toString()
        } else {
            value.asDouble().toString()
        }

    private fun formatSequence(
        sequence: SequenceValue,
        limit: Int = 20,
    ): String {
        val size = sequence.size
        if (size == 0L) return "{}"
        val displayCount = minOf(size, limit.toLong()).toInt()
        val builder = StringBuilder()
        builder.append('{')
        for (i in 0 until displayCount) {
            if (i > 0) builder.append(", ")
            builder.append(formatNumber(sequence.get(i.toLong())))
        }
        if (size > limit) {
            builder.append(", ..., ")
            builder.append(formatNumber(sequence.get(size - 1)))
        }
        builder.append('}')
        return builder.toString()
    }
}
