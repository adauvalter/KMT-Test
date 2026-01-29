package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.diagnostics.SourceSpan

/**
 * Runtime error reported during evaluation with its source span.
 */
class EvaluationError(
    message: String,
    val span: SourceSpan,
) : RuntimeException(message)
