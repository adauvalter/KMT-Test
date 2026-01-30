package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.evaluation.RangeSequence
import com.jetbrains.kmt.lang.evaluation.SequenceOperations
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ParallelReduceTest {
    @Test
    fun reducesLargeRangeCorrectly() =
        runTest {
            val sequence = RangeSequence(1, 100_000)
            val result =
                SequenceOperations().reduce(sequence, NumberValue.fromInt(0)) { accumulator, value ->
                    NumberValue.fromInt(accumulator.asLong() + value.asLong())
                }
            val expected = 100_000L * 100_001L / 2
            assertEquals(expected, result.asLong())
        }
}
