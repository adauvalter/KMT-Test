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

class ExpressionEvaluatorCoverageTest {
    @Test
    fun binaryOperatorRejectsNonNumberOperands() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val expression =
                BoundExpression.Binary(
                    BoundExpression.SequenceLiteral(
                        BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                        BoundExpression.NumberLiteral(NumberValue.fromInt(2), span),
                        span,
                    ),
                    Operators.PLUS,
                    BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                    NumberType.IntType,
                    span,
                )
            val evaluator = ExpressionEvaluator()
            assertFailsWith<EvaluationError> {
                evaluator.evaluate(expression, emptyMap())
            }
        }

    @Test
    fun sequenceLiteralRejectsDoubleBounds() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val expression =
                BoundExpression.SequenceLiteral(
                    BoundExpression.NumberLiteral(NumberValue.fromDouble(1.5), span),
                    BoundExpression.NumberLiteral(NumberValue.fromInt(2), span),
                    span,
                )
            val evaluator = ExpressionEvaluator()
            assertFailsWith<EvaluationError> {
                evaluator.evaluate(expression, emptyMap())
            }
        }

    @Test
    fun mapCallRejectsNonSequence() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val expression =
                BoundExpression.MapCall(
                    sequence = BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                    parameterName = "i",
                    body = BoundExpression.Variable("i", NumberType.IntType, span),
                    type = SequenceType(NumberType.IntType),
                    span = span,
                )
            val evaluator = ExpressionEvaluator()
            assertFailsWith<EvaluationError> {
                evaluator.evaluate(expression, emptyMap())
            }
        }

    @Test
    fun mapCallUsesLambdaEvaluatorForNonPureExpression() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val nestedReduce =
                BoundExpression.ReduceCall(
                    sequence =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            span,
                        ),
                    neutral = BoundExpression.NumberLiteral(NumberValue.fromInt(0), span),
                    accumulatorParameter = "x",
                    elementParameter = "y",
                    body =
                        BoundExpression.Binary(
                            BoundExpression.Variable("x", NumberType.IntType, span),
                            Operators.PLUS,
                            BoundExpression.Variable("y", NumberType.IntType, span),
                            NumberType.IntType,
                            span,
                        ),
                    type = NumberType.IntType,
                    span = span,
                )
            val mapExpression =
                BoundExpression.MapCall(
                    sequence =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(2), span),
                            span,
                        ),
                    parameterName = "i",
                    body = nestedReduce,
                    type = SequenceType(NumberType.IntType),
                    span = span,
                )
            val evaluator = ExpressionEvaluator()
            val result = evaluator.evaluate(mapExpression, emptyMap())
            val sequence = result as com.jetbrains.kmt.lang.evaluation.SequenceValue
            assertEquals(1L, sequence.get(0).asLong())
            assertEquals(1L, sequence.get(1).asLong())
        }

    @Test
    fun mapCallReportsUnknownParameterInPureLambda() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val mapExpression =
                BoundExpression.MapCall(
                    sequence =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            span,
                        ),
                    parameterName = "i",
                    body = BoundExpression.Variable("x", NumberType.IntType, span),
                    type = SequenceType(NumberType.IntType),
                    span = span,
                )
            val evaluator = ExpressionEvaluator()
            assertFailsWith<EvaluationError> {
                evaluator.evaluate(mapExpression, emptyMap())
            }
        }

    @Test
    fun reduceRejectsNonNumberNeutral() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val reduceExpression =
                BoundExpression.ReduceCall(
                    sequence =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(2), span),
                            span,
                        ),
                    neutral =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(0), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            span,
                        ),
                    accumulatorParameter = "x",
                    elementParameter = "y",
                    body =
                        BoundExpression.Binary(
                            BoundExpression.Variable("x", NumberType.IntType, span),
                            Operators.PLUS,
                            BoundExpression.Variable("y", NumberType.IntType, span),
                            NumberType.IntType,
                            span,
                        ),
                    type = NumberType.IntType,
                    span = span,
                )
            val evaluator = ExpressionEvaluator()
            assertFailsWith<EvaluationError> {
                evaluator.evaluate(reduceExpression, emptyMap())
            }
        }

    @Test
    fun reduceUsesLambdaEvaluatorForNonPureExpression() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val nestedReduce =
                BoundExpression.ReduceCall(
                    sequence =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            span,
                        ),
                    neutral = BoundExpression.NumberLiteral(NumberValue.fromInt(0), span),
                    accumulatorParameter = "a",
                    elementParameter = "b",
                    body =
                        BoundExpression.Binary(
                            BoundExpression.Variable("a", NumberType.IntType, span),
                            Operators.PLUS,
                            BoundExpression.Variable("b", NumberType.IntType, span),
                            NumberType.IntType,
                            span,
                        ),
                    type = NumberType.IntType,
                    span = span,
                )
            val reduceExpression =
                BoundExpression.ReduceCall(
                    sequence =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(2), span),
                            span,
                        ),
                    neutral = BoundExpression.NumberLiteral(NumberValue.fromInt(0), span),
                    accumulatorParameter = "x",
                    elementParameter = "y",
                    body = nestedReduce,
                    type = NumberType.IntType,
                    span = span,
                )
            val evaluator = ExpressionEvaluator()
            val result = evaluator.evaluate(reduceExpression, emptyMap()) as NumberValue
            assertEquals(1L, result.asLong())
        }

    @Test
    fun reduceReportsUnknownParameterInPureLambda() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val reduceExpression =
                BoundExpression.ReduceCall(
                    sequence =
                        BoundExpression.SequenceLiteral(
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            BoundExpression.NumberLiteral(NumberValue.fromInt(1), span),
                            span,
                        ),
                    neutral = BoundExpression.NumberLiteral(NumberValue.fromInt(0), span),
                    accumulatorParameter = "x",
                    elementParameter = "y",
                    body = BoundExpression.Variable("z", NumberType.IntType, span),
                    type = NumberType.IntType,
                    span = span,
                )
            val evaluator = ExpressionEvaluator()
            assertFailsWith<EvaluationError> {
                evaluator.evaluate(reduceExpression, emptyMap())
            }
        }
}
