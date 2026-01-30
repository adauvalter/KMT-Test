package com.jetbrains.kmt.lang.semantics

import com.jetbrains.kmt.lang.ast.BoundExpression
import com.jetbrains.kmt.lang.ast.BoundProgram
import com.jetbrains.kmt.lang.ast.BoundStatement
import com.jetbrains.kmt.lang.ast.Expression
import com.jetbrains.kmt.lang.ast.Program
import com.jetbrains.kmt.lang.ast.Statement
import com.jetbrains.kmt.lang.diagnostics.Diagnostic
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.syntax.Operators

/**
 * Binds parsed AST nodes to a typed representation and reports semantic diagnostics.
 */
class Binder {
    private val diagnostics = mutableListOf<Diagnostic>()
    private val globals = mutableMapOf<String, Type>()

    /**
     * Binds a parsed [Program] into a [BoundProgram].
     */
    fun bind(program: Program): BindResult {
        val boundStatements = mutableListOf<BoundStatement>()
        for (statement in program.statements) {
            when (statement) {
                is Statement.VariableDeclaration -> {
                    val bound = bindExpression(statement.expression, globals)
                    if (bound != null) {
                        globals[statement.name] = bound.type
                        boundStatements +=
                            BoundStatement.VariableDeclaration(
                                name = statement.name,
                                expression = bound,
                                span = statement.span,
                            )
                    }
                }

                is Statement.Out -> {
                    val bound = bindExpression(statement.expression, globals)
                    if (bound != null) {
                        boundStatements += BoundStatement.Out(bound, statement.span)
                    }
                }

                is Statement.Print -> {
                    boundStatements += BoundStatement.Print(statement.text, statement.span)
                }
            }
        }
        return BindResult(BoundProgram(boundStatements, globals.toMap()), diagnostics)
    }

    private fun bindExpression(
        expression: Expression,
        env: Map<String, Type>,
    ): BoundExpression? {
        return when (expression) {
            is Expression.NumberLiteral -> {
                parseNumberLiteral(expression)
            }

            is Expression.Identifier -> {
                val type = env[expression.name]
                if (type == null) {
                    diagnostics += Diagnostic("Undefined variable '${expression.name}'", expression.span)
                    null
                } else {
                    BoundExpression.Variable(expression.name, type, expression.span)
                }
            }

            is Expression.Unary -> {
                val boundExpression = bindExpression(expression.expression, env) ?: return null
                val numberType =
                    requireNumber(boundExpression, "Unary '${expression.op}' expects a number") ?: return null
                BoundExpression.Unary(expression.op, boundExpression, numberType, expression.span)
            }

            is Expression.Group -> {
                bindExpression(expression.expression, env)
            }

            is Expression.Binary -> {
                val left = bindExpression(expression.left, env) ?: return null
                val right = bindExpression(expression.right, env) ?: return null
                val leftType = requireNumber(left, "Operator '${expression.operator}' expects numbers") ?: return null
                val rightType = requireNumber(right, "Operator '${expression.operator}' expects numbers") ?: return null
                val resultType =
                    when (expression.operator) {
                        Operators.DIVIDE, Operators.POWER -> NumberType.DoubleType
                        Operators.PLUS, Operators.MINUS, Operators.MULTIPLY -> mergeNumberTypes(leftType, rightType)
                        else -> leftType
                    }
                BoundExpression.Binary(left, expression.operator, right, resultType, expression.span)
            }

            is Expression.SequenceLiteral -> {
                val start = bindExpression(expression.start, env) ?: return null
                val end = bindExpression(expression.end, env) ?: return null
                if (start.type != NumberType.IntType || end.type != NumberType.IntType) {
                    diagnostics += Diagnostic("Sequence bounds must be integers", expression.span)
                    return null
                }
                BoundExpression.SequenceLiteral(start, end, expression.span)
            }

            is Expression.MapCall -> {
                val sequence = bindExpression(expression.sequence, env) ?: return null
                if (sequence.type !is SequenceType) {
                    diagnostics += Diagnostic("map expects a sequence", expression.sequence.span)
                    return null
                }
                val elementType = (sequence.type as SequenceType).elementType
                val localEnv = mapOf(expression.parameterName to elementType)
                val body = bindExpression(expression.body, localEnv) ?: return null
                val bodyType = requireNumber(body, "map lambda must return a number") ?: return null
                BoundExpression.MapCall(
                    sequence = sequence,
                    parameterName = expression.parameterName,
                    body = body,
                    type = SequenceType(bodyType),
                    span = expression.span,
                )
            }

            is Expression.ReduceCall -> {
                val sequence = bindExpression(expression.sequence, env) ?: return null
                if (sequence.type !is SequenceType) {
                    diagnostics += Diagnostic("reduce expects a sequence", expression.sequence.span)
                    return null
                }
                val neutral = bindExpression(expression.neutral, env) ?: return null
                val neutralType = requireNumber(neutral, "reduce neutral element must be a number") ?: return null
                val elementType = (sequence.type as SequenceType).elementType
                val localEnv =
                    mapOf(
                        expression.accumulatorParameter to neutralType,
                        expression.elementParameter to elementType,
                    )
                val body = bindExpression(expression.body, localEnv) ?: return null
                val bodyType = requireNumber(body, "reduce lambda must return a number") ?: return null
                val resultType =
                    resolveAccumulatorType(neutralType, bodyType, expression.body.span) ?: return null
                BoundExpression.ReduceCall(
                    sequence = sequence,
                    neutral = neutral,
                    accumulatorParameter = expression.accumulatorParameter,
                    elementParameter = expression.elementParameter,
                    body = body,
                    type = resultType,
                    span = expression.span,
                )
            }
        }
    }

    private fun requireNumber(
        expression: BoundExpression,
        message: String,
    ): NumberType? =
        if (expression.type is NumberType) {
            expression.type as NumberType
        } else {
            diagnostics += Diagnostic(message, expression.span)
            null
        }

    private fun parseNumberLiteral(expression: Expression.NumberLiteral): BoundExpression? =
        if (expression.isInt) {
            val value = expression.text.toLongOrNull()
            if (value == null) {
                diagnostics += Diagnostic("Integer literal is out of range", expression.span)
                null
            } else {
                BoundExpression.NumberLiteral(NumberValue.fromInt(value), expression.span)
            }
        } else {
            val value = expression.text.toDoubleOrNull()
            if (value == null || value.isNaN() || value.isInfinite()) {
                diagnostics += Diagnostic("Real literal is out of range", expression.span)
                null
            } else {
                BoundExpression.NumberLiteral(NumberValue.fromDouble(value), expression.span)
            }
        }

    private fun resolveAccumulatorType(
        accumulatorType: NumberType,
        resultType: NumberType,
        span: SourceSpan,
    ): NumberType? =
        when {
            accumulatorType == NumberType.DoubleType -> {
                NumberType.DoubleType
            }

            resultType == NumberType.DoubleType -> {
                NumberType.DoubleType
            }

            accumulatorType == resultType -> {
                accumulatorType
            }

            else -> {
                diagnostics += Diagnostic("reduce lambda result must match accumulator type", span)
                null
            }
        }
}

/**
 * Result of binding: a bound program and diagnostics.
 */
data class BindResult(
    val program: BoundProgram,
    val diagnostics: List<Diagnostic>,
)
