package com.jetbrains.kmt.lang.ast

import com.jetbrains.kmt.lang.diagnostics.SourceSpan

/**
 * Base type for all parsed expressions.
 */
sealed interface Expression {
    val span: SourceSpan

    /**
     * Numeric literal expression.
     */
    data class NumberLiteral(
        val text: String,
        val isInt: Boolean,
        override val span: SourceSpan,
    ) : Expression

    /**
     * Identifier reference expression.
     */
    data class Identifier(
        val name: String,
        override val span: SourceSpan,
    ) : Expression

    /**
     * Unary operator expression.
     */
    data class Unary(
        val op: String,
        val expression: Expression,
        override val span: SourceSpan,
    ) : Expression

    /**
     * Parenthesized expression.
     */
    data class Group(
        val expression: Expression,
        override val span: SourceSpan,
    ) : Expression

    /**
     * Binary operator expression.
     */
    data class Binary(
        val left: Expression,
        val operator: String,
        val right: Expression,
        override val span: SourceSpan,
    ) : Expression

    /**
     * Sequence literal expression of the form {start, end}.
     */
    data class SequenceLiteral(
        val start: Expression,
        val end: Expression,
        override val span: SourceSpan,
    ) : Expression

    /**
     * Map call expression.
     */
    data class MapCall(
        val sequence: Expression,
        val parameterName: String,
        val body: Expression,
        override val span: SourceSpan,
    ) : Expression

    /**
     * Reduce call expression.
     */
    data class ReduceCall(
        val sequence: Expression,
        val neutral: Expression,
        val accumulatorParameter: String,
        val elementParameter: String,
        val body: Expression,
        override val span: SourceSpan,
    ) : Expression
}

/**
 * Base type for all parsed statements.
 */
sealed interface Statement {
    val span: SourceSpan

    /**
     * Variable declaration statement.
     */
    data class VariableDeclaration(
        val name: String,
        val expression: Expression,
        override val span: SourceSpan,
    ) : Statement

    /**
     * Output expression statement.
     */
    data class Out(
        val expression: Expression,
        override val span: SourceSpan,
    ) : Statement

    /**
     * Print string literal statement.
     */
    data class Print(
        val text: String,
        override val span: SourceSpan,
    ) : Statement
}

/**
 * Root program node containing top-level statements.
 */
data class Program(
    val statements: List<Statement>,
)
