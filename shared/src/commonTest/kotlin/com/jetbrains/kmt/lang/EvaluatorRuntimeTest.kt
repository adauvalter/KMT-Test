package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.api.Analyzer
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.ArraySequence
import com.jetbrains.kmt.lang.evaluation.EvaluationError
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.evaluation.RangeSequence
import com.jetbrains.kmt.lang.evaluation.SequenceOperations
import com.jetbrains.kmt.lang.evaluation.ValueChecks
import com.jetbrains.kmt.lang.evaluation.ValueFormatter
import com.jetbrains.kmt.lang.semantics.NumberType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluatorRuntimeTest {
    @Test
    fun evaluatesMapAndReduce() =
        runTest {
            val program =
                """
                var seq = map({1, 3}, i -> i * 2)
                out reduce(seq, 0, x y -> x + y)
                """.trimIndent()
            val result = Analyzer.analyze(program)
            assertTrue(result.diagnostics.isEmpty())
            assertEquals("12\n", result.output)
        }

    @Test
    fun sequenceOperationsMapDetectsDouble() =
        runTest {
            val sequence = RangeSequence(1, 2)
            val mapped =
                SequenceOperations().map(
                    sequence,
                    { value -> NumberValue.fromDouble(value.asDouble() / 2.0) },
                    NumberType.DoubleType,
                    SourceSpan(0, 0, 1, 1),
                )
            assertEquals(NumberType.DoubleType, mapped.elementType)
            assertEquals(0.5, mapped.get(0).asDouble())
        }

    @Test
    fun valueFormatterEllipsis() =
        runTest {
            val data = DoubleArray(25) { it.toDouble() }
            val sequence = ArraySequence(NumberType.DoubleType, data)
            val formatted = ValueFormatter().formatValue(sequence)
            assertTrue(formatted.contains("..."))
            assertTrue(formatted.startsWith("{"))
        }

    @Test
    fun valueChecksRejectWrongType() {
        val checks = ValueChecks()
        try {
            checks.requireSequence(NumberValue.fromInt(1), "expected sequence", SourceSpan(0, 1, 1, 1))
        } catch (e: EvaluationError) {
            assertEquals("expected sequence", e.message)
            return
        }
        kotlin.test.fail("Expected EvaluationError")
    }
}
