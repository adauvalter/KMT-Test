package com.jetbrains.kmt.lang.ast

import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.evaluation.NumberValue
import com.jetbrains.kmt.lang.semantics.NumberType
import com.jetbrains.kmt.lang.semantics.SequenceType
import com.jetbrains.kmt.lang.semantics.Type

/**
 * Base type for bound expressions that carry type information.
 */
sealed interface BoundExpression {
    val type: Type
    val span: SourceSpan

    /**
     * Bound numeric literal.
     */
    data class NumberLiteral(
        val value: NumberValue,
        override val span: SourceSpan,
    ) : BoundExpression {
        override val type: Type = value.kind
    }

    /**
     * Bound variable reference.
     */
    data class Variable(
        val name: String,
        override val type: Type,
        override val span: SourceSpan,
    ) : BoundExpression

    /**
     * Bound unary operator expression.
     */
    data class Unary(
        val op: String,
        val expression: BoundExpression,
        override val type: Type,
        override val span: SourceSpan,
    ) : BoundExpression

    /**
     * Bound binary operator expression.
     */
    data class Binary(
        val left: BoundExpression,
        val op: String,
        val right: BoundExpression,
        override val type: Type,
        override val span: SourceSpan,
    ) : BoundExpression

    /**
     * Bound sequence literal.
     */
    data class SequenceLiteral(
        val start: BoundExpression,
        val end: BoundExpression,
        override val span: SourceSpan,
    ) : BoundExpression {
        override val type: Type = SequenceType(NumberType.IntType)
    }

    /**
     * Bound map call.
     */
    data class MapCall(
        val sequence: BoundExpression,
        val parameterName: String,
        val body: BoundExpression,
        override val type: Type,
        override val span: SourceSpan,
    ) : BoundExpression

    /**
     * Bound reduce call.
     */
    data class ReduceCall(
        val sequence: BoundExpression,
        val neutral: BoundExpression,
        val accumulatorParameter: String,
        val elementParameter: String,
        val body: BoundExpression,
        override val type: Type,
        override val span: SourceSpan,
    ) : BoundExpression
}

/**
 * Base type for bound statements.
 */
sealed interface BoundStatement {
    val span: SourceSpan

    /**
     * Bound variable declaration.
     */
    data class VarDecl(
        val name: String,
        val expression: BoundExpression,
        override val span: SourceSpan,
    ) : BoundStatement

    /**
     * Bound output statement.
     */
    data class Out(
        val expression: BoundExpression,
        override val span: SourceSpan,
    ) : BoundStatement

    /**
     * Bound print statement.
     */
    data class Print(
        val text: String,
        override val span: SourceSpan,
    ) : BoundStatement
}

/**
 * Root node for a bound program.
 */
data class BoundProgram(
    val statements: List<BoundStatement>,
    val globals: Map<String, Type>,
)
