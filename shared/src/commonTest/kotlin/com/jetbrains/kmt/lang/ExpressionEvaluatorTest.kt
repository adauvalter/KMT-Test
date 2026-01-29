package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.ast.BoundExpression
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.EvaluationError
import com.jetbrains.kmt.lang.evaluation.ExpressionEvaluator
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.semantics.NumberType
import com.jetbrains.kmt.lang.semantics.SequenceType
import com.jetbrains.kmt.lang.syntax.Operators
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExpressionEvaluatorTest {
    @Test
    fun evaluatesUnaryMinus() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val expression =
                BoundExpression.Unary(
                    Operators.MINUS,
                    BoundExpression.NumberLiteral(NumberValue.fromInt(3), span),
                    NumberType.IntType,
                    span,
                )
            val evaluator = ExpressionEvaluator()
            val result = evaluator.evaluate(expression, emptyMap())
            assertEquals(-3L, (result as NumberValue).asLong())
        }

    @Test
    fun errorsOnUndefinedVariable() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val expression = BoundExpression.Variable("missing", NumberType.IntType, span)
            val evaluator = ExpressionEvaluator()
            assertFailsWith<EvaluationError> {
                evaluator.evaluate(expression, emptyMap())
            }
        }

    @Test
    fun evaluatesReduceWithMapFusion() =
        runTest {
            val program =
                """
                out reduce(map({1, 3}, i -> i + 1), 0, x y -> x + y)
                """.trimIndent()
            val result = com.jetbrains.kmt.lang.api.Analyzer.analyze(program)
            assertEquals("9\n", result.output)
        }

    @Test
    fun evaluatesUnaryWithDouble() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val expression =
                BoundExpression.Unary(
                    Operators.MINUS,
                    BoundExpression.NumberLiteral(NumberValue.fromDouble(2.5), span),
                    NumberType.DoubleType,
                    span,
                )
            val evaluator = ExpressionEvaluator()
            val result = evaluator.evaluate(expression, emptyMap())
            assertEquals(-2.5, (result as NumberValue).asDouble())
        }

    @Test
    fun evaluatesSequenceLiteral() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val expression =
                BoundExpression.SequenceLiteral(
                    BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                    BoundExpression.NumberLiteral(NumberValue.fromInt(2), span),
                    span,
                )
            val evaluator = ExpressionEvaluator()
            val result = evaluator.evaluate(expression, emptyMap())
            val sequence = result as com.jetbrains.kmt.lang.evaluation.SequenceValue
            assertEquals(2L, sequence.size)
            assertEquals(1L, sequence.get(0).asLong())
        }

    @Test
    fun evaluatesMapCall() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val mapExpression =
                BoundExpression.MapCall(
                    sequence =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(2), span),
                            span,
                        ),
                    parameterName = "i",
                    body =
                        BoundExpression.Binary(
                            BoundExpression.Variable("i", NumberType.IntType, span),
                            Operators.PLUS,
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            NumberType.IntType,
                            span,
                        ),
                    type = SequenceType(NumberType.IntType),
                    span = span,
                )
            val evaluator = ExpressionEvaluator()
            val result = evaluator.evaluate(mapExpression, emptyMap())
            val sequence = result as com.jetbrains.kmt.lang.evaluation.SequenceValue
            assertEquals(2L, sequence.size)
            assertEquals(2L, sequence.get(0).asLong())
        }
}
