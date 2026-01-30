package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.semantics.NumberType
import com.jetbrains.kmt.lang.syntax.Operators
import kotlin.math.pow

/**
 * Implements numeric operator semantics.
 */
class NumberOperations {
    /**
     * Applies a unary operator to a numeric value.
     */
    fun unary(
        op: String,
        value: NumberValue,
        span: SourceSpan,
    ): NumberValue =
        when (op) {
            Operators.MINUS -> {
                if (value.kind == NumberType.IntType) {
                    NumberValue.fromInt(-value.asLong())
                } else {
                    NumberValue.fromDouble(-value.asDouble())
                }
            }

            else -> {
                throw EvaluationError("Unknown operator '$op'", span)
            }
        }

    /**
     * Applies a binary operator to two numeric values.
     */
    fun binary(
        left: NumberValue,
        right: NumberValue,
        op: String,
        span: SourceSpan,
    ): NumberValue =
        when (op) {
            Operators.PLUS -> add(left, right)
            Operators.MINUS -> subtract(left, right)
            Operators.MULTIPLY -> multiply(left, right)
            Operators.DIVIDE -> NumberValue.fromDouble(left.asDouble() / right.asDouble())
            Operators.POWER -> NumberValue.fromDouble(left.asDouble().pow(right.asDouble()))
            else -> throw EvaluationError("Unknown operator '$op'", span)
        }

    private fun add(
        left: NumberValue,
        right: NumberValue,
    ): NumberValue =
        if (left.kind == NumberType.IntType && right.kind == NumberType.IntType) {
            NumberValue.fromInt(left.asLong() + right.asLong())
        } else {
            NumberValue.fromDouble(left.asDouble() + right.asDouble())
        }

    private fun subtract(
        left: NumberValue,
        right: NumberValue,
    ): NumberValue =
        if (left.kind == NumberType.IntType && right.kind == NumberType.IntType) {
            NumberValue.fromInt(left.asLong() - right.asLong())
        } else {
            NumberValue.fromDouble(left.asDouble() - right.asDouble())
        }

    private fun multiply(
        left: NumberValue,
        right: NumberValue,
    ): NumberValue =
        if (left.kind == NumberType.IntType && right.kind == NumberType.IntType) {
            NumberValue.fromInt(left.asLong() * right.asLong())
        } else {
            NumberValue.fromDouble(left.asDouble() * right.asDouble())
        }
}
