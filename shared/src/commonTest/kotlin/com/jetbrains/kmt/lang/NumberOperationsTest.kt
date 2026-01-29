package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.EvaluationError
import com.jetbrains.kmt.lang.evaluation.NumberOperations
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.syntax.Operators
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NumberOperationsTest {
    @Test
    fun unaryMinusHandlesIntAndDouble() {
        val span = SourceSpan(0, 1, 1, 1)
        val ops = NumberOperations()
        assertEquals(-3L, ops.unary(Operators.MINUS, NumberValue.fromInt(3), span).asLong())
        assertEquals(-2.5, ops.unary(Operators.MINUS, NumberValue.fromDouble(2.5), span).asDouble())
    }

    @Test
    fun powerUsesDoubleMath() {
        val span = SourceSpan(0, 1, 1, 1)
        val ops = NumberOperations()
        val result = ops.binary(NumberValue.fromInt(2), NumberValue.fromInt(3), Operators.POWER, span)
        assertEquals(8.0, result.asDouble())
    }

    @Test
    fun unknownOperatorThrows() {
        val span = SourceSpan(0, 1, 1, 1)
        val ops = NumberOperations()
        assertFailsWith<EvaluationError> {
            ops.binary(NumberValue.fromInt(1), NumberValue.fromInt(2), "%", span)
        }
    }
}
