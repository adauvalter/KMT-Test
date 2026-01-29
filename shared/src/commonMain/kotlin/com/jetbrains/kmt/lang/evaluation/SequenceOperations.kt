package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.semantics.NumberType

/**
 * Sequence operations that may execute in parallel.
 */
class SequenceOperations {
    /**
     * Maps each element of a sequence, returning a new sequence.
     */
    suspend fun map(
        sequence: SequenceValue,
        mapper: suspend (NumberValue) -> NumberValue,
        elementType: NumberType,
        span: SourceSpan,
    ): SequenceValue {
        return parallelMap(sequence, mapper, elementType)
    }

    /**
     * Reduces a sequence into a single value.
     */
    suspend fun reduce(
        sequence: SequenceValue,
        neutral: NumberValue,
        reducer: suspend (NumberValue, NumberValue) -> NumberValue,
    ): NumberValue {
        return parallelReduce(sequence, neutral, reducer)
    }

    /**
     * Maps each element and reduces the result in a single pass.
     */
    suspend fun mapReduce(
        sequence: SequenceValue,
        mapper: suspend (NumberValue) -> NumberValue,
        neutral: NumberValue,
        reducer: suspend (NumberValue, NumberValue) -> NumberValue,
    ): NumberValue {
        return parallelMapReduce(sequence, mapper, neutral, reducer)
    }
}
