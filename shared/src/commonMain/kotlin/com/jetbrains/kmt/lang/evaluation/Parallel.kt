package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.semantics.NumberType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.min

private const val DEFAULT_CHUNK_SIZE = 10_000

/**
 * Maps a sequence in parallel and materializes the result.
 */
suspend fun parallelMap(
    sequence: SequenceValue,
    mapper: suspend (NumberValue) -> NumberValue,
    elementType: NumberType,
    chunkSize: Int = DEFAULT_CHUNK_SIZE,
): ArraySequence {
    val size = sequence.size
    if (size > Int.MAX_VALUE) {
        throw RuntimeException("Sequence too large to materialize: ${size - 1}")
    }
    val result = DoubleArray(size.toInt())
    if (size == 0L) {
        return ArraySequence(elementType, result)
    }
    coroutineScope { mapInParallel(sequence, size, chunkSize, mapper, result) }
    return ArraySequence(elementType, result)
}

/**
 * Reduces a sequence in parallel using an associative reducer.
 */
suspend fun parallelReduce(
    sequence: SequenceValue,
    neutral: NumberValue,
    reducer: suspend (NumberValue, NumberValue) -> NumberValue,
    chunkSize: Int = DEFAULT_CHUNK_SIZE,
): NumberValue {
    val size = sequence.size
    if (size == 0L) return neutral
    if (size <= chunkSize) {
        var accumulatorValue = neutral
        var i = 0L
        while (i < size) {
            accumulatorValue = reducer(accumulatorValue, sequence.get(i))
            i++
        }
        return accumulatorValue
    }

    return coroutineScope { reduceInParallel(sequence, size, chunkSize, neutral, reducer) }
}

/**
 * Maps and reduces a sequence in parallel without materializing the mapped values.
 */
suspend fun parallelMapReduce(
    sequence: SequenceValue,
    mapper: suspend (NumberValue) -> NumberValue,
    neutral: NumberValue,
    reducer: suspend (NumberValue, NumberValue) -> NumberValue,
    chunkSize: Int = DEFAULT_CHUNK_SIZE,
): NumberValue {
    val size = sequence.size
    if (size == 0L) return neutral
    if (size <= chunkSize) {
        var accumulatorValue = neutral
        var i = 0L
        while (i < size) {
            val mapped = mapper(sequence.get(i))
            accumulatorValue = reducer(accumulatorValue, mapped)
            i++
        }
        return accumulatorValue
    }
    return coroutineScope { reduceMappedInParallel(sequence, size, chunkSize, mapper, neutral, reducer) }
}

private suspend fun mapInParallel(
    sequence: SequenceValue,
    size: Long,
    chunkSize: Int,
    mapper: suspend (NumberValue) -> NumberValue,
    result: DoubleArray,
) {
    return coroutineScope {
        val jobs = mutableListOf<kotlinx.coroutines.Deferred<Unit>>()
        var start = 0L
        while (start < size) {
            val end = min(size, start + chunkSize)
            val chunkStart = start
            jobs +=
                async(Dispatchers.Default) {
                    var i = chunkStart
                    while (i < end) {
                        val value = mapper(sequence.get(i))
                        result[i.toInt()] = value.doubleValue
                        i++
                    }
                }
            start = end
        }
        jobs.awaitAll()
    }
}

private suspend fun reduceInParallel(
    sequence: SequenceValue,
    size: Long,
    chunkSize: Int,
    neutral: NumberValue,
    reducer: suspend (NumberValue, NumberValue) -> NumberValue,
): NumberValue {
    return coroutineScope {
        val jobs = mutableListOf<kotlinx.coroutines.Deferred<NumberValue>>()
        var start = 0L
        while (start < size) {
            val end = min(size, start + chunkSize)
            val chunkStart = start
            jobs +=
                async(Dispatchers.Default) {
                    var accumulatorValue = neutral
                    var i = chunkStart
                    while (i < end) {
                        accumulatorValue = reducer(accumulatorValue, sequence.get(i))
                        i++
                    }
                    accumulatorValue
                }
            start = end
        }
        val chunkResults = jobs.awaitAll()
        var finalAccumulator = neutral
        for (chunk in chunkResults) {
            finalAccumulator = reducer(finalAccumulator, chunk)
        }
        finalAccumulator
    }
}

private suspend fun reduceMappedInParallel(
    sequence: SequenceValue,
    size: Long,
    chunkSize: Int,
    mapper: suspend (NumberValue) -> NumberValue,
    neutral: NumberValue,
    reducer: suspend (NumberValue, NumberValue) -> NumberValue,
): NumberValue {
    return coroutineScope {
        val jobs = mutableListOf<kotlinx.coroutines.Deferred<NumberValue>>()
        var start = 0L
        while (start < size) {
            val end = min(size, start + chunkSize)
            val chunkStart = start
            jobs +=
                async(Dispatchers.Default) {
                    var accumulatorValue = neutral
                    var i = chunkStart
                    while (i < end) {
                        val mapped = mapper(sequence.get(i))
                        accumulatorValue = reducer(accumulatorValue, mapped)
                        i++
                    }
                    accumulatorValue
                }
            start = end
        }
        val chunkResults = jobs.awaitAll()
        var finalAccumulator = neutral
        for (chunk in chunkResults) {
            finalAccumulator = reducer(finalAccumulator, chunk)
        }
        finalAccumulator
    }
}
