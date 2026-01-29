package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.ast.BoundExpression

/**
 * Evaluates lambda bodies with a local environment.
 */
class LambdaEvaluator(
    private val evaluator: suspend (BoundExpression, Map<String, Value>) -> Value,
) {
    /**
     * Evaluates a single-parameter lambda body.
     */
    suspend fun evaluateSingle(
        parameterName: String,
        value: NumberValue,
        body: BoundExpression,
    ): NumberValue {
        val localEnv = mapOf(parameterName to value)
        val result = evaluator(body, localEnv)
        return result as? NumberValue
            ?: throw EvaluationError("Lambda must return a number", body.span)
    }

    /**
     * Evaluates a two-parameter lambda body.
     */
    suspend fun evaluateDouble(
        accumulatorParameterName: String,
        accumulatorValue: NumberValue,
        elementParameterName: String,
        elementValue: NumberValue,
        body: BoundExpression,
    ): NumberValue {
        val localEnv =
            mapOf(
                accumulatorParameterName to accumulatorValue,
                elementParameterName to elementValue,
            )
        val result = evaluator(body, localEnv)
        return result as? NumberValue
            ?: throw EvaluationError("Lambda must return a number", body.span)
    }
}
