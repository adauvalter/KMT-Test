package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.ast.BoundExpression
import com.jetbrains.kmt.lang.semantics.SequenceType

/**
 * Evaluates bound expressions to runtime values.
 */
class ExpressionEvaluator(
    private val numberOperations: NumberOperations = NumberOperations(),
    private val sequenceOperations: SequenceOperations = SequenceOperations(),
    private val checks: ValueChecks = ValueChecks(),
    private val sequenceFactory: SequenceFactory = SequenceFactory(),
) {
    private val lambdaEvaluator = LambdaEvaluator(::evaluate)

    /**
     * Evaluates a bound [expression] within the provided environment.
     */
    suspend fun evaluate(
        expression: BoundExpression,
        env: Map<String, Value>,
    ): Value {
        return when (expression) {
            is BoundExpression.NumberLiteral -> expression.value
            is BoundExpression.Variable ->
                env[expression.name]
                    ?: throw EvaluationError("Undefined variable '${expression.name}'", expression.span)
            is BoundExpression.Unary -> {
                val value =
                    checks.requireNumber(
                        evaluate(expression.expression, env),
                        "Unary '${expression.op}' expects a number",
                        expression.span,
                    )
                numberOperations.unary(expression.op, value, expression.span)
            }
            is BoundExpression.Binary -> {
                val left =
                    checks.requireNumber(
                        evaluate(expression.left, env),
                        "Operator '${expression.op}' expects numbers",
                        expression.span,
                    )
                val right =
                    checks.requireNumber(
                        evaluate(expression.right, env),
                        "Operator '${expression.op}' expects numbers",
                        expression.span,
                    )
                numberOperations.binary(left, right, expression.op, expression.span)
            }
            is BoundExpression.SequenceLiteral -> {
                val start =
                    checks.requireNumber(
                        evaluate(expression.start, env),
                        "Sequence bounds must be integers",
                        expression.span,
                    )
                val end =
                    checks.requireNumber(
                        evaluate(expression.end, env),
                        "Sequence bounds must be integers",
                        expression.span,
                    )
                sequenceFactory.range(start, end, expression.span)
            }
            is BoundExpression.MapCall -> {
                val sequence =
                    checks.requireSequence(
                        evaluate(expression.sequence, env),
                        "map expects a sequence",
                        expression.sequence.span,
                    )
                val mapper: suspend (NumberValue) -> NumberValue =
                    if (isPureNumeric(expression.body)) {
                        { value -> evaluateNumeric(expression.body, expression.parameterName, value) }
                    } else {
                        { value -> lambdaEvaluator.evaluateSingle(expression.parameterName, value, expression.body) }
                    }
                val elementType = (expression.type as SequenceType).elementType
                sequenceOperations.map(sequence, mapper, elementType, expression.span)
            }
            is BoundExpression.ReduceCall -> {
                if (expression.sequence is BoundExpression.MapCall) {
                    val mapExpression = expression.sequence as BoundExpression.MapCall
                    val sequence =
                        checks.requireSequence(
                            evaluate(mapExpression.sequence, env),
                            "map expects a sequence",
                            mapExpression.sequence.span,
                        )
                    val neutral =
                        checks.requireNumber(
                            evaluate(expression.neutral, env),
                            "reduce neutral element must be a number",
                            expression.neutral.span,
                        )
                    val mapper: suspend (NumberValue) -> NumberValue =
                        if (isPureNumeric(mapExpression.body)) {
                            { value -> evaluateNumeric(mapExpression.body, mapExpression.parameterName, value) }
                        } else {
                            { value -> lambdaEvaluator.evaluateSingle(mapExpression.parameterName, value, mapExpression.body) }
                        }
                    val reducer: suspend (NumberValue, NumberValue) -> NumberValue =
                        if (isPureNumeric(expression.body)) {
                            { accumulatorValue, elementValue ->
                                evaluateNumeric(
                                    expression.body,
                                    expression.accumulatorParameter,
                                    accumulatorValue,
                                    expression.elementParameter,
                                    elementValue,
                                )
                            }
                        } else {
                            { accumulatorValue, elementValue ->
                                lambdaEvaluator.evaluateDouble(
                                    expression.accumulatorParameter,
                                    accumulatorValue,
                                    expression.elementParameter,
                                    elementValue,
                                    expression.body,
                                )
                            }
                        }
                    sequenceOperations.mapReduce(sequence, mapper, neutral, reducer)
                } else {
                    val sequence =
                        checks.requireSequence(
                            evaluate(expression.sequence, env),
                            "reduce expects a sequence",
                            expression.sequence.span,
                        )
                    val neutral =
                        checks.requireNumber(
                            evaluate(expression.neutral, env),
                            "reduce neutral element must be a number",
                            expression.neutral.span,
                        )
                    val reducer: suspend (NumberValue, NumberValue) -> NumberValue =
                        if (isPureNumeric(expression.body)) {
                            { accumulatorValue, elementValue ->
                                evaluateNumeric(
                                    expression.body,
                                    expression.accumulatorParameter,
                                    accumulatorValue,
                                    expression.elementParameter,
                                    elementValue,
                                )
                            }
                        } else {
                            { accumulatorValue, elementValue ->
                                lambdaEvaluator.evaluateDouble(
                                    expression.accumulatorParameter,
                                    accumulatorValue,
                                    expression.elementParameter,
                                    elementValue,
                                    expression.body,
                                )
                            }
                        }
                    sequenceOperations.reduce(sequence, neutral, reducer)
                }
            }
        }
    }

    private fun isPureNumeric(expression: BoundExpression): Boolean {
        return when (expression) {
            is BoundExpression.NumberLiteral -> true
            is BoundExpression.Variable -> true
            is BoundExpression.Unary -> isPureNumeric(expression.expression)
            is BoundExpression.Binary -> isPureNumeric(expression.left) && isPureNumeric(expression.right)
            is BoundExpression.SequenceLiteral -> false
            is BoundExpression.MapCall -> false
            is BoundExpression.ReduceCall -> false
        }
    }

    private fun evaluateNumeric(
        expression: BoundExpression,
        parameterName: String,
        value: NumberValue,
    ): NumberValue {
        return when (expression) {
            is BoundExpression.NumberLiteral -> expression.value
            is BoundExpression.Variable ->
                if (expression.name == parameterName) {
                    value
                } else {
                    throw EvaluationError("Undefined variable '${expression.name}'", expression.span)
                }
            is BoundExpression.Unary -> {
                val operand = evaluateNumeric(expression.expression, parameterName, value)
                numberOperations.unary(expression.op, operand, expression.span)
            }
            is BoundExpression.Binary -> {
                val left = evaluateNumeric(expression.left, parameterName, value)
                val right = evaluateNumeric(expression.right, parameterName, value)
                numberOperations.binary(left, right, expression.op, expression.span)
            }
            is BoundExpression.SequenceLiteral,
            is BoundExpression.MapCall,
            is BoundExpression.ReduceCall,
            -> throw EvaluationError("Unsupported expression in numeric lambda", expression.span)
        }
    }

    private fun evaluateNumeric(
        expression: BoundExpression,
        accumulatorName: String,
        accumulatorValue: NumberValue,
        elementName: String,
        elementValue: NumberValue,
    ): NumberValue {
        return when (expression) {
            is BoundExpression.NumberLiteral -> expression.value
            is BoundExpression.Variable ->
                when (expression.name) {
                    accumulatorName -> accumulatorValue
                    elementName -> elementValue
                    else -> throw EvaluationError("Undefined variable '${expression.name}'", expression.span)
                }
            is BoundExpression.Unary -> {
                val operand = evaluateNumeric(expression.expression, accumulatorName, accumulatorValue, elementName, elementValue)
                numberOperations.unary(expression.op, operand, expression.span)
            }
            is BoundExpression.Binary -> {
                val left = evaluateNumeric(expression.left, accumulatorName, accumulatorValue, elementName, elementValue)
                val right = evaluateNumeric(expression.right, accumulatorName, accumulatorValue, elementName, elementValue)
                numberOperations.binary(left, right, expression.op, expression.span)
            }
            is BoundExpression.SequenceLiteral,
            is BoundExpression.MapCall,
            is BoundExpression.ReduceCall,
            -> throw EvaluationError("Unsupported expression in numeric lambda", expression.span)
        }
    }
}
