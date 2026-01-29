package com.jetbrains.kmt.lang

import com.jetbrains.kmt.lang.semantics.Binder
import com.jetbrains.kmt.lang.syntax.Lexer
import com.jetbrains.kmt.lang.syntax.Parser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiagnosticSpanTest {
    @Test
    fun binderUndefinedVariableHasSpan() {
        val source = "out x"
        val program = Parser(Lexer(source).lex().tokens).parseProgram().program
        val bind = Binder().bind(program)
        assertTrue(bind.diagnostics.isNotEmpty())
        val diagnostic = bind.diagnostics.first()
        assertEquals(1, diagnostic.span.line)
        assertEquals(5, diagnostic.span.column)
    }
}
