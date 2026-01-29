package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.semantics.NumberType

/**
 * Runtime type checks for evaluated values.
 */
class ValueChecks {
    /**
     * Ensures that a value is numeric or throws an [EvaluationError].
     */
    fun requireNumber(
        value: Value,
        message: String,
        span: SourceSpan,
    ): NumberValue {
        return value as? NumberValue ?: throw EvaluationError(message, span)
    }

    /**
     * Ensures that a value is a sequence or throws an [EvaluationError].
     */
    fun requireSequence(
        value: Value,
        message: String,
        span: SourceSpan,
    ): SequenceValue {
        return value as? SequenceValue ?: throw EvaluationError(message, span)
    }
}

/**
 * Builds runtime sequences from evaluated bounds.
 */
class SequenceFactory {
    /**
     * Creates an integer range sequence from two bound values.
     */
    fun range(
        start: NumberValue,
        end: NumberValue,
        span: SourceSpan,
    ): SequenceValue {
        if (start.kind != NumberType.IntType || end.kind != NumberType.IntType) {
            throw EvaluationError("Sequence bounds must be integers", span)
        }
        val startVal = start.asLong()
        val endVal = end.asLong()
        if (startVal > endVal) {
            throw EvaluationError("Sequence start must be <= end", span)
        }
        val length = endVal - startVal
        if (length < 0 || length == Long.MAX_VALUE) {
            throw EvaluationError("Sequence range is too large", span)
        }
        return RangeSequence(startVal, endVal)
    }
}
