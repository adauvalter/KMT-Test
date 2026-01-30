package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.EvaluationError
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.evaluation.SequenceFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SequenceFactoryTest {
    @Test
    fun rejectsNonIntegerBounds() {
        val span = SourceSpan(0, 1, 1, 1)
        val factory = SequenceFactory()
        val error =
            assertFailsWith<EvaluationError> {
                factory.range(NumberValue.fromDouble(1.2), NumberValue.fromInt(2), span)
            }
        assertEquals("Sequence bounds must be integers", error.message)
    }

    @Test
    fun rejectsDescendingBounds() {
        val span = SourceSpan(0, 1, 1, 1)
        val factory = SequenceFactory()
        val error =
            assertFailsWith<EvaluationError> {
                factory.range(NumberValue.fromInt(3), NumberValue.fromInt(1), span)
            }
        assertEquals("Sequence start must be <= end", error.message)
    }

    @Test
    fun rejectsRangesThatOverflowLength() {
        val span = SourceSpan(0, 1, 1, 1)
        val factory = SequenceFactory()
        val error =
            assertFailsWith<EvaluationError> {
                factory.range(NumberValue.fromInt(0), NumberValue.fromInt(Long.MAX_VALUE), span)
            }
        assertEquals("Sequence range is too large", error.message)
    }
}
