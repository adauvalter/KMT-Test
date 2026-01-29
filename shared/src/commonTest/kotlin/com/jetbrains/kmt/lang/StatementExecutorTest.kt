package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.ast.BoundExpression
import com.jetbrains.kmt.lang.ast.BoundStatement
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.EvaluationContext
import com.jetbrains.kmt.lang.evaluation.ExpressionEvaluator
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.evaluation.StatementExecutor
import com.jetbrains.kmt.lang.evaluation.ValueFormatter
import com.jetbrains.kmt.lang.semantics.NumberType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatementExecutorTest {
    @Test
    fun executesVarOutAndPrintStatements() =
        runTest {
            val span = SourceSpan(0, 1, 1, 1)
            val number = BoundExpression.NumberLiteral(NumberValue.fromInt(7), span)
            val variable = BoundExpression.Variable("x", NumberType.IntType, span)
            val executor = StatementExecutor(ExpressionEvaluator(), ValueFormatter())
            val context = EvaluationContext(mutableMapOf(), StringBuilder())

            executor.execute(BoundStatement.VarDecl("x", number, span), context)
            executor.execute(BoundStatement.Out(variable, span), context)
            executor.execute(BoundStatement.Print("done", span), context)

            assertTrue(context.globals.containsKey("x"))
            assertEquals("7\ndone", context.output.toString())
        }
}
