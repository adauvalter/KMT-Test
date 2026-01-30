package com.jetbrains.kmt.lang.api

import com.jetbrains.kmt.lang.diagnostics.Diagnostic
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.EvaluationError
import com.jetbrains.kmt.lang.evaluation.Interpreter
import com.jetbrains.kmt.lang.semantics.Binder
import com.jetbrains.kmt.lang.syntax.Lexer
import com.jetbrains.kmt.lang.syntax.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs lexing, parsing, type-checking, and evaluation for a source program.
 */
object Analyzer {
    /**
     * Analyzes a source program and returns the resulting output and diagnostics.
     */
    suspend fun analyze(source: String): AnalysisResult =
        withContext(Dispatchers.Default) {
            val lex = Lexer(source).lex()
            if (lex.diagnostics.isNotEmpty()) {
                return@withContext AnalysisResult(diagnostics = lex.diagnostics)
            }
            val parse = Parser(lex.tokens).parseProgram()
            val bind = Binder().bind(parse.program)
            val diagnostics = parse.diagnostics + bind.diagnostics
            if (diagnostics.isNotEmpty()) {
                return@withContext AnalysisResult(diagnostics = diagnostics)
            }
            return@withContext try {
                val evaluation = Interpreter().evaluate(bind.program)
                AnalysisResult(output = evaluation.output)
            } catch (err: EvaluationError) {
                AnalysisResult(diagnostics = listOf(Diagnostic(err.message ?: "Runtime error", err.span)))
            } catch (err: CancellationException) {
                throw err
            } catch (err: RuntimeException) {
                AnalysisResult(
                    diagnostics = listOf(Diagnostic(err.message ?: "Runtime error", SourceSpan(0, 0, 1, 1))),
                )
            }
        }
}

/**
 * Aggregates the analysis output and any diagnostics for a source program.
 */
data class AnalysisResult(
    val output: String = "",
    val diagnostics: List<Diagnostic> = emptyList(),
)
