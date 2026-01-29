package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.ast.Expression
import com.jetbrains.kmt.lang.ast.Statement
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.diagnostics.mergeSpan
import com.jetbrains.kmt.lang.syntax.Lexer
import com.jetbrains.kmt.lang.syntax.Operators
import com.jetbrains.kmt.lang.syntax.Parser
import com.jetbrains.kmt.lang.syntax.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LexerParserTest {
    @Test
    fun lexesTokensWithSpans() {
        val source = "var x = 1\nprint \"hi\""
        val result = Lexer(source).lex()
        assertTrue(result.diagnostics.isEmpty())
        assertEquals("var", result.tokens.first().text)
        val stringToken = result.tokens.first { it.type == TokenType.String }
        assertEquals('"', stringToken.text.first())
        assertEquals(2, stringToken.span.line)
    }

    @Test
    fun parsesOperatorPrecedenceAndGrouping() {
        val source = "out 1 + 2 * (3 + 4)"
        val tokens = Lexer(source).lex().tokens
        val program = Parser(tokens).parseProgram().program
        val out = program.statements.first() as Statement.Out
        val expression = out.expression as Expression.Binary
        assertEquals(Operators.PLUS, expression.operator)
        val right = expression.right as Expression.Binary
        assertEquals(Operators.MULTIPLY, right.operator)
        val grouped = right.right as Expression.Group
        val inner = grouped.expression as Expression.Binary
        assertEquals(Operators.PLUS, inner.operator)
    }

    @Test
    fun reportsInvalidCharacter() {
        val source = "out 1 $ 2"
        val result = Lexer(source).lex()
        assertTrue(result.diagnostics.isNotEmpty())
    }

    @Test
    fun mergesSpans() {
        val left = SourceSpan(0, 1, 1, 1)
        val right = SourceSpan(2, 2, 1, 3)
        val merged = mergeSpan(left, right)
        assertEquals(0, merged.startOffset)
        assertEquals(4, merged.length)
    }
}
