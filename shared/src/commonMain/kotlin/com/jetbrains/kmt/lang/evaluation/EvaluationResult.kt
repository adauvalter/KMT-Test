package com.jetbrains.kmt.lang.evaluation

/**
 * Result of program evaluation.
 */
data class EvaluationResult(
    val output: String,
    val globals: Map<String, Value>,
)
