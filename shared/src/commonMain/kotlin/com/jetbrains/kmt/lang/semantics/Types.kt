package com.jetbrains.kmt.lang.semantics

/**
 * Base type for the language type system.
 */
sealed interface Type

/**
 * Numeric types.
 */
sealed interface NumberType : Type {
    /** Integer number type. */
    object IntType : NumberType

    /** Floating-point number type. */
    object DoubleType : NumberType
}

/**
 * Sequence type with element type information.
 */
data class SequenceType(
    val elementType: NumberType,
) : Type

/**
 * Returns the resulting numeric type after combining two numeric operands.
 */
fun mergeNumberTypes(
    left: NumberType,
    right: NumberType,
): NumberType =
    if (left == NumberType.DoubleType || right == NumberType.DoubleType) {
        NumberType.DoubleType
    } else {
        NumberType.IntType
    }
