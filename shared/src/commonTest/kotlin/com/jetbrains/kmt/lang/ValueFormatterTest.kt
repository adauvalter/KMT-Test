package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.evaluation.RangeSequence
import com.jetbrains.kmt.lang.evaluation.ValueFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValueFormatterTest {
    @Test
    fun formatsDoubleWithDecimalPart() {
        val formatted = ValueFormatter().formatValue(NumberValue.fromDouble(18.0))
        assertEquals("18.0", formatted)
    }

    @Test
    fun formatsSequenceWithEllipsisAndLastValue() {
        val formatted = ValueFormatter().formatValue(RangeSequence(1, 30))
        assertTrue(formatted.contains("...,"))
        assertTrue(formatted.endsWith("30}"))
    }
}
