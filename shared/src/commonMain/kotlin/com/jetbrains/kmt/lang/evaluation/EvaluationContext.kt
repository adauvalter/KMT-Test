package com.jetbrains.kmt.lang.evaluation

/**
 * Mutable evaluation context with globals and output buffer.
 */
data class EvaluationContext(
    val globals: MutableMap<String, Value>,
    val output: StringBuilder,
)
