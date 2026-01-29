package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.ArraySequence
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.evaluation.RangeSequence
import com.jetbrains.kmt.lang.evaluation.SequenceOperations
import com.jetbrains.kmt.lang.semantics.NumberType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SequenceOperationsTest {
    @Test
    fun reduceEmptySequenceReturnsNeutral() =
        runTest {
            val empty = ArraySequence(NumberType.IntType, DoubleArray(0))
            val result =
                SequenceOperations().reduce(empty, NumberValue.fromInt(5)) { accumulator, value ->
                    NumberValue.fromInt(accumulator.asLong() + value.asLong())
                }
            assertEquals(5L, result.asLong())
        }

    @Test
    fun mapHandlesChunkBoundaries() =
        runTest {
            val sequence = RangeSequence(0, 25_000)
            val mapped =
                SequenceOperations().map(
                    sequence,
                    { value -> NumberValue.fromInt(value.asLong() + 1) },
                    NumberType.IntType,
                    SourceSpan(0, 0, 1, 1),
                )
            assertEquals(25_001L, mapped.size)
            assertEquals(1L, mapped.get(0).asLong())
            assertEquals(25_001L, mapped.get(mapped.size - 1).asLong())
        }

    @Test
    fun rangeSequenceIndexesCorrectly() =
        runTest {
            val range = RangeSequence(-2, 2)
            assertEquals(5L, range.size)
            assertEquals(-2L, range.get(0).asLong())
            assertEquals(2L, range.get(4).asLong())
        }

    @Test
    fun arraySequenceFormatsIntType() =
        runTest {
            val array = ArraySequence(NumberType.IntType, doubleArrayOf(1.0, 2.0))
            assertEquals(1L, array.get(0).asLong())
            assertEquals(2L, array.get(1).asLong())
        }
}
