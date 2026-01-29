package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.ast.Expression
import com.jetbrains.kmt.lang.ast.Statement
import com.jetbrains.kmt.lang.syntax.Lexer
import com.jetbrains.kmt.lang.syntax.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserDiagnosticsTest {
    @Test
    fun reportsMissingParenthesis() {
        val source = "out (1 + 2"
        val result = Parser(Lexer(source).lex().tokens).parseProgram()
        assertTrue(result.diagnostics.isNotEmpty())
        assertTrue(result.diagnostics.first().message.contains(")"))
    }

    @Test
    fun recoversAfterInvalidStatement() {
        val source = "out 1 +\nvar x = 2\nout x"
        val result = Parser(Lexer(source).lex().tokens).parseProgram()
        assertTrue(result.diagnostics.isNotEmpty())
        assertEquals(2, result.program.statements.size)
        val second = result.program.statements[1] as Statement.Out
        val expression = second.expression as Expression.Identifier
        assertEquals("x", expression.name)
    }

    @Test
    fun diagnosticsHaveSpans() {
        val source = "out @"
        val lex = Lexer(source).lex()
        assertTrue(lex.diagnostics.isNotEmpty())
        val diag = lex.diagnostics.first()
        assertEquals(1, diag.span.line)
        assertEquals(5, diag.span.column)
    }
}
