package com.jetbrains.kmt.lang.ast

import com.jetbrains.kmt.lang.ast.Expression
import com.jetbrains.kmt.lang.ast.Program
import com.jetbrains.kmt.lang.ast.Statement
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import kotlin.test.Test
import kotlin.test.assertEquals

class AstSmokeTest {
    @Test
    fun constructsExpressionAndStatementNodes() {
        val span = SourceSpan(0, 1, 1, 1)
        val number = Expression.NumberLiteral("1", true, span)
        val identifier = Expression.Identifier("x", span)
        val unary = Expression.Unary("-", number, span)
        val group = Expression.Group(unary, span)
        val binary = Expression.Binary(number, "+", identifier, span)
        val sequence = Expression.SequenceLiteral(number, identifier, span)
        val mapCall = Expression.MapCall(sequence, "i", binary, span)
        val reduceCall = Expression.ReduceCall(mapCall, number, "a", "b", binary, span)

        assertEquals("-", unary.op)
        assertEquals("x", identifier.name)
        assertEquals(sequence, mapCall.sequence)
        assertEquals("a", reduceCall.accumulatorParameter)
        assertEquals("b", reduceCall.elementParameter)

        val variableDeclaration = Statement.VariableDeclaration("x", number, span)
        val out = Statement.Out(identifier, span)
        val print = Statement.Print("hi", span)
        val program = Program(listOf(variableDeclaration, out, print))

        assertEquals(3, program.statements.size)
        assertEquals(unary, group.expression)
    }
}
