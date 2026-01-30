package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.api.Analyzer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class RegressionProgramTest {
    @Test
    fun reportsUndefinedVariableAlongsideSyntaxError() =
        runTest {
            val program =
                """
                out pirr
                hhfhfhfhgf
                """.trimIndent()
            val result = Analyzer.analyze(program)
            assertTrue(result.diagnostics.size >= 2)
            assertTrue(result.diagnostics.any { it.message.contains("Undefined variable") })
            assertTrue(result.diagnostics.any { it.message.contains("statement") })
        }

    @Test
    fun reportsMapOnNonSequence() =
        runTest {
            val result = Analyzer.analyze("out map(1, i -> i)")
            assertTrue(result.diagnostics.any { it.message.contains("map expects a sequence") })
        }

    @Test
    fun reportsReduceOnNonSequence() =
        runTest {
            val result = Analyzer.analyze("out reduce(1, 0, x y -> x + y)")
            assertTrue(result.diagnostics.any { it.message.contains("reduce expects a sequence") })
        }

    @Test
    fun reportsMapLambdaReturningSequence() =
        runTest {
            val result = Analyzer.analyze("out map({1, 2}, i -> {1, 2})")
            assertTrue(result.diagnostics.any { it.message.contains("map lambda must return a number") })
        }

    @Test
    fun reportsReduceLambdaReturningSequence() =
        runTest {
            val result = Analyzer.analyze("out reduce({1, 2}, 0, x y -> {1, 2})")
            assertTrue(result.diagnostics.any { it.message.contains("reduce lambda must return a number") })
        }

    @Test
    fun reportsIntegerLiteralOutOfRange() =
        runTest {
            val result = Analyzer.analyze("var n = 999999999999999999999999999999")
            assertTrue(result.diagnostics.any { it.message.contains("Integer literal is out of range") })
        }

    @Test
    fun reportsNonIntegerSequenceBounds() =
        runTest {
            val result = Analyzer.analyze("out {1.2, 3}")
            assertTrue(result.diagnostics.any { it.message.contains("Sequence bounds must be integers") })
        }

    @Test
    fun reportsDescendingSequenceBoundsAtRuntime() =
        runTest {
            val result = Analyzer.analyze("out {2, 1}")
            assertTrue(result.diagnostics.any { it.message.contains("Sequence start must be <= end") })
        }
}
