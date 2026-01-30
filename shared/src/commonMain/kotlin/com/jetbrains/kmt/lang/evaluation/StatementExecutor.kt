package com.jetbrains.kmt.lang.evaluation

import com.jetbrains.kmt.lang.ast.BoundStatement

/**
 * Executes bound statements and updates the evaluation context.
 */
class StatementExecutor(
    private val expressionEvaluator: ExpressionEvaluator,
    private val formatter: ValueFormatter,
) {
    /**
     * Executes a single bound [statement].
     */
    suspend fun execute(
        statement: BoundStatement,
        context: EvaluationContext,
    ) {
        when (statement) {
            is BoundStatement.VariableDeclaration -> {
                val value = expressionEvaluator.evaluate(statement.expression, context.globals)
                context.globals[statement.name] = value
            }

            is BoundStatement.Out -> {
                val value = expressionEvaluator.evaluate(statement.expression, context.globals)
                context.output.append(formatter.formatValue(value)).append('\n')
            }

            is BoundStatement.Print -> {
                context.output.append(statement.text)
            }
        }
    }
}
