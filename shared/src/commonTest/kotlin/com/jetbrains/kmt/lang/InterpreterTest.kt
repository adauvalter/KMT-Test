package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.api.Analyzer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterpreterTest {
    @Test
    fun evaluatesOperatorPrecedence() =
        runTest {
            val result = Analyzer.analyze("out 1 + 2 * 3")
            assertTrue(result.diagnostics.isEmpty())
            assertEquals("7\n", result.output)
        }

    @Test
    fun buildsAndMapsSequence() =
        runTest {
            val program =
                """
                var seq = map({1, 3}, i -> i * 2)
                out seq
                """.trimIndent()
            val result = Analyzer.analyze(program)
            assertTrue(result.diagnostics.isEmpty())
            assertEquals("{2, 4, 6}\n", result.output)
        }

    @Test
    fun reducesSequenceWithNeutral() =
        runTest {
            val program = "out reduce({1, 3}, 0, x y -> x + y)"
            val result = Analyzer.analyze(program)
            assertTrue(result.diagnostics.isEmpty())
            assertEquals("6\n", result.output)
        }

    @Test
    fun rejectsNonIntegerSequenceBounds() =
        runTest {
            val program = "out {1.2, 3}"
            val result = Analyzer.analyze(program)
            assertTrue(result.diagnostics.isNotEmpty())
            assertTrue(result.diagnostics.first().message.contains("Sequence bounds"))
        }

    @Test
    fun lambdaCannotAccessGlobals() =
        runTest {
            val program =
                """
                var n = 2
                var seq = map({1, 2}, i -> i + n)
                out seq
                """.trimIndent()
            val result = Analyzer.analyze(program)
            assertTrue(result.diagnostics.isNotEmpty())
            assertTrue(result.diagnostics.first().message.contains("Undefined variable"))
        }

    @Test
    fun reducePromotesAccumulatorToDouble() =
        runTest {
            val program = "out reduce({1, 2}, 1, x y -> x / y)"
            val result = Analyzer.analyze(program)
            assertTrue(result.diagnostics.isEmpty())
            assertEquals("0.5\n", result.output)
        }
}
