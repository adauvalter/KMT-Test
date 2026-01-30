package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.evaluation.RangeSequence
import com.jetbrains.kmt.lang.evaluation.parallelMap
import com.jetbrains.kmt.lang.evaluation.parallelMapReduce
import com.jetbrains.kmt.lang.evaluation.parallelReduce
import com.jetbrains.kmt.lang.semantics.NumberType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelOperationsTest {
    @Test
    fun parallelMapHandlesMultipleChunks() =
        runTest {
            val sequence = RangeSequence(0, 5)
            val mapped =
                parallelMap(
                    sequence = sequence,
                    mapper = { value -> NumberValue.fromInt(value.asLong() + 1) },
                    elementType = NumberType.IntType,
                    chunkSize = 2,
                )
            assertEquals(6L, mapped.size)
            assertEquals(1L, mapped.get(0).asLong())
            assertEquals(6L, mapped.get(5).asLong())
        }

    @Test
    fun parallelReduceHandlesMultipleChunks() =
        runTest {
            val sequence = RangeSequence(1, 5)
            val result =
                parallelReduce(
                    sequence = sequence,
                    neutral = NumberValue.fromInt(0),
                    reducer = { accumulator, value ->
                        NumberValue.fromInt(accumulator.asLong() + value.asLong())
                    },
                    chunkSize = 2,
                )
            assertEquals(15L, result.asLong())
        }

    @Test
    fun parallelReduceHandlesEmptySequence() =
        runTest {
            val sequence = RangeSequence(1, 0)
            val result =
                parallelReduce(
                    sequence = sequence,
                    neutral = NumberValue.fromInt(42),
                    reducer = { accumulator, value ->
                        NumberValue.fromInt(accumulator.asLong() + value.asLong())
                    },
                    chunkSize = 2,
                )
            assertEquals(42L, result.asLong())
        }

    @Test
    fun parallelMapReduceHandlesMultipleChunks() =
        runTest {
            val sequence = RangeSequence(1, 4)
            val result =
                parallelMapReduce(
                    sequence = sequence,
                    mapper = { value -> NumberValue.fromInt(value.asLong() * 2) },
                    neutral = NumberValue.fromInt(0),
                    reducer = { accumulator, value ->
                        NumberValue.fromInt(accumulator.asLong() + value.asLong())
                    },
                    chunkSize = 2,
                )
            assertEquals(20L, result.asLong())
        }
}
