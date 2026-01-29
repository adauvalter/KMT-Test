package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.semantics.NumberType

/**
 * Base type for runtime values.
 */
sealed interface Value

/**
 * Numeric runtime value with both integer and double representations.
 */
data class NumberValue(
    val kind: NumberType,
    val doubleValue: Double,
    val longValue: Long,
) : Value {
    companion object {
        fun fromInt(value: Long): NumberValue = NumberValue(NumberType.IntType, value.toDouble(), value)

        fun fromDouble(value: Double): NumberValue = NumberValue(NumberType.DoubleType, value, value.toLong())
    }

    fun asDouble(): Double = doubleValue

    fun asLong(): Long = longValue
}

/**
 * Runtime sequence value interface.
 */
interface SequenceValue : Value {
    val elementType: NumberType
    val size: Long

    fun get(index: Long): NumberValue
}

/**
 * Sequence backed by an integer range.
 */
class RangeSequence(
    private val start: Long,
    endInclusive: Long,
) : SequenceValue {
    override val elementType: NumberType = NumberType.IntType
    override val size: Long = (endInclusive - start + 1).coerceAtLeast(0)

    override fun get(index: Long): NumberValue = NumberValue.fromInt(start + index)
}

/**
 * Sequence backed by an in-memory array.
 */
class ArraySequence(
    override val elementType: NumberType,
    private val data: DoubleArray,
) : SequenceValue {
    override val size: Long = data.size.toLong()

    override fun get(index: Long): NumberValue {
        val value = data[index.toInt()]
        return if (elementType == NumberType.IntType) NumberValue.fromInt(value.toLong()) else NumberValue.fromDouble(value)
    }
}
