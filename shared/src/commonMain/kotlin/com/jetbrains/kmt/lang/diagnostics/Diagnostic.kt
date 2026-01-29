package com.jetbrains.kmt.lang.diagnostics

/**
 * Severity level of a [Diagnostic].
 */
enum class Severity { Error, Warning }

/**
 * A diagnostic message emitted during lexing, parsing, binding, or evaluation.
 */
data class Diagnostic(
    val message: String,
    val span: SourceSpan,
    val severity: Severity = Severity.Error,
)
