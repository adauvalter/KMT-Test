package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.ast.BoundProgram

/**
 * Executes a bound program and produces output plus globals.
 */
class Interpreter(
    private val expressionEvaluator: ExpressionEvaluator = ExpressionEvaluator(),
    private val statementExecutor: StatementExecutor = StatementExecutor(expressionEvaluator, ValueFormatter()),
) {
    /**
     * Evaluates a [BoundProgram] to an [EvaluationResult].
     */
    suspend fun evaluate(program: BoundProgram): EvaluationResult {
        val context = EvaluationContext(mutableMapOf(), StringBuilder())
        for (statement in program.statements) {
            statementExecutor.execute(statement, context)
        }
        return EvaluationResult(context.output.toString(), context.globals.toMap())
    }
}
