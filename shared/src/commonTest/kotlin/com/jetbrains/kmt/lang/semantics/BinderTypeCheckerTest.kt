package com.jetbrains.kmt.lang.semantics

import com.jetbrains.kmt.lang.ast.BoundStatement
import com.jetbrains.kmt.lang.semantics.Binder
import com.jetbrains.kmt.lang.semantics.NumberType
import com.jetbrains.kmt.lang.syntax.Lexer
import com.jetbrains.kmt.lang.syntax.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BinderTypeCheckerTest {
    @Test
    fun binderPromotesReduceAccumulatorToDouble() {
        val program = "var pi = reduce({1, 2}, 0, x y -> x / y)"
        val parser = Parser(Lexer(program).lex().tokens)
        val bound = Binder().bind(parser.parseProgram().program)
        assertTrue(bound.diagnostics.isEmpty())
        val stmt = bound.program.statements.first() as BoundStatement.VariableDeclaration
        assertEquals(NumberType.DoubleType, stmt.expression.type)
    }

    @Test
    fun binderRejectsSequenceBounds() {
        val program = "out {1.2, 3}"
        val bound = Binder().bind(Parser(Lexer(program).lex().tokens).parseProgram().program)
        assertTrue(bound.diagnostics.isNotEmpty())
        assertTrue(
            bound.diagnostics
                .first()
                .message
                .contains("Sequence bounds"),
        )
    }

    @Test
    fun binderRejectsHugeIntegerLiteral() {
        val program = "var n = 999999999999999999999999999999"
        val bound = Binder().bind(Parser(Lexer(program).lex().tokens).parseProgram().program)
        assertTrue(bound.diagnostics.isNotEmpty())
        assertEquals("Integer literal is out of range", bound.diagnostics.first().message)
    }
}
