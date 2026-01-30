package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.ast.BoundExpression
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.ArraySequence
import com.jetbrains.kmt.lang.evaluation.EvaluationError
import com.jetbrains.kmt.lang.evaluation.LambdaEvaluator
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.semantics.NumberType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LambdaEvaluatorTest {
    @Test
    fun evaluateSingleReturnsNumber() =
        runTest {
            val evaluator =
                LambdaEvaluator { _, _ ->
                    NumberValue.fromInt(3)
                }
            val body = BoundExpression.NumberLiteral(NumberValue.fromInt(3), SourceSpan(0, 1, 1, 1))
            val result = evaluator.evaluateSingle("x", NumberValue.fromInt(1), body)
            assertEquals(3L, result.asLong())
        }

    @Test
    fun evaluateSingleRejectsNonNumber() =
        runTest {
            val evaluator =
                LambdaEvaluator { _, _ ->
                    ArraySequence(NumberType.IntType, doubleArrayOf(1.0))
                }
            val body = BoundExpression.NumberLiteral(NumberValue.fromInt(1), SourceSpan(0, 1, 1, 1))
            assertFailsWith<EvaluationError> {
                evaluator.evaluateSingle("x", NumberValue.fromInt(1), body)
            }
        }

    @Test
    fun evaluateDoubleRejectsNonNumber() =
        runTest {
            val evaluator =
                LambdaEvaluator { _, _ ->
                    ArraySequence(NumberType.IntType, doubleArrayOf(1.0))
                }
            val body = BoundExpression.NumberLiteral(NumberValue.fromInt(1), SourceSpan(0, 1, 1, 1))
            assertFailsWith<EvaluationError> {
                evaluator.evaluateDouble(
                    "a",
                    NumberValue.fromInt(1),
                    "b",
                    NumberValue.fromInt(2),
                    body,
                )
            }
        }
}
