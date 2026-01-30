package com.jetbrains.kmt.lang.syntax

import com.jetbrains.kmt.lang.ast.Expression
import com.jetbrains.kmt.lang.ast.Program
import com.jetbrains.kmt.lang.ast.Statement
import com.jetbrains.kmt.lang.diagnostics.Diagnostic
import com.jetbrains.kmt.lang.diagnostics.SourceSpan
import com.jetbrains.kmt.lang.diagnostics.mergeSpan

/**
 * Builds an abstract syntax tree from a token stream and reports syntax diagnostics.
 *
 * The parser consumes tokens produced by [Lexer] and produces a [Program] plus any
 * syntax errors encountered during parsing. Parsing is resilient: when an error is
 * detected it attempts to recover to the next statement boundary.
 */
class Parser(
    private val tokens: List<Token>,
) {
    private val diagnostics = mutableListOf<Diagnostic>()
    private var index = 0

    /**
     * Parses an entire program (a sequence of statements) until EOF.
     */
    fun parseProgram(): ParseResult {
        val statements = mutableListOf<Statement>()
        while (!isAtEnd()) {
            if (match(TokenType.Eof)) break
            val statement = parseStatement()
            if (statement != null) {
                statements += statement
            } else {
                recoverToNextStatement()
            }
        }
        return ParseResult(Program(statements), diagnostics)
    }

    /**
     * Parses a single top-level statement.
     */
    private fun parseStatement(): Statement? {
        return when {
            matchKeyword(Keywords.VAR) -> {
                val nameToken = expect(TokenType.Identifier, ParserMessages.EXPECTED_IDENTIFIER_AFTER_VAR) ?: return null
                expectSymbol(Symbols.EQUALS, ParserMessages.EXPECTED_EQUALS_AFTER_VAR) ?: return null
                val expression = parseExpression(0) ?: return null
                Statement.VariableDeclaration(nameToken.text, expression, mergeSpan(nameToken.span, expression.span))
            }

            matchKeyword(Keywords.OUT) -> {
                val expression = parseExpression(0) ?: return null
                Statement.Out(expression, mergeSpan(previous().span, expression.span))
            }

            matchKeyword(Keywords.PRINT) -> {
                val stringToken = expect(TokenType.String, ParserMessages.EXPECTED_STRING_AFTER_PRINT) ?: return null
                val content = unescapeString(stringToken.text)
                Statement.Print(content, stringToken.span)
            }

            else -> {
                diagnostics += Diagnostic(ParserMessages.EXPECTED_STATEMENT, current().span)
                null
            }
        }
    }

    /**
     * Parses an expression using precedence climbing.
     *
     * @param minPrecedence the minimum precedence level that the caller allows.
     */
    private fun parseExpression(minPrecedence: Int): Expression? {
        var left = parsePrefix() ?: return null
        while (true) {
            val opToken = current()
            if (opToken.type != TokenType.Operator) break
            val precedence = precedence(opToken.text)
            val assocRight = opToken.text == Operators.POWER
            if (precedence < minPrecedence) break
            advance()
            val nextMin = if (assocRight) precedence else precedence + 1
            val right = parseExpression(nextMin) ?: return null
            left = Expression.Binary(left, opToken.text, right, mergeSpan(left.span, right.span))
        }
        return left
    }

    /**
     * Parses prefix operators (currently unary minus) before delegating to primary parsing.
     */
    private fun parsePrefix(): Expression? {
        if (matchMinus()) {
            val expression = parseExpression(4) ?: return null
            return Expression.Unary(Operators.MINUS, expression, mergeSpan(previous().span, expression.span))
        }
        return parsePrimary()
    }

    /**
     * Parses primary expressions: literals, identifiers, groupings, sequences, map/reduce calls.
     */
    private fun parsePrimary(): Expression? {
        val token = current()
        return when {
            match(TokenType.Number) -> {
                val text = token.text
                val isInt = !text.contains('.')
                Expression.NumberLiteral(text, isInt, token.span)
            }

            match(TokenType.Identifier) -> {
                Expression.Identifier(token.text, token.span)
            }

            matchSymbol(Symbols.LEFT_PAREN) -> {
                val expression = parseExpression(0) ?: return null
                val close =
                    expectSymbol(Symbols.RIGHT_PAREN, ParserMessages.EXPECTED_RIGHT_PAREN_AFTER_EXPRESSION)
                        ?: return null
                Expression.Group(expression, mergeSpan(token.span, close.span))
            }

            matchSymbol(Symbols.LEFT_BRACE) -> {
                val startExpression = parseExpression(0) ?: return null
                expectSymbol(Symbols.COMMA, ParserMessages.EXPECTED_COMMA_IN_SEQUENCE) ?: return null
                val endExpression = parseExpression(0) ?: return null
                expectSymbol(Symbols.RIGHT_BRACE, ParserMessages.EXPECTED_RIGHT_BRACE_AFTER_SEQUENCE) ?: return null
                Expression.SequenceLiteral(startExpression, endExpression, mergeSpan(token.span, endExpression.span))
            }

            matchKeyword(Keywords.MAP) -> {
                parseMapCall(token.span)
            }

            matchKeyword(Keywords.REDUCE) -> {
                parseReduceCall(token.span)
            }

            else -> {
                diagnostics += Diagnostic(ParserMessages.EXPECTED_EXPRESSION, token.span)
                null
            }
        }
    }

    /**
     * Parses a map call expression starting after the `map` keyword.
     */
    private fun parseMapCall(startSpan: SourceSpan): Expression? {
        expectSymbol(Symbols.LEFT_PAREN, ParserMessages.EXPECTED_LEFT_PAREN_AFTER_MAP) ?: return null
        val sequence = parseExpression(0) ?: return null
        expectSymbol(Symbols.COMMA, ParserMessages.EXPECTED_COMMA_AFTER_MAP_SEQUENCE) ?: return null
        val parameterToken =
            expect(TokenType.Identifier, ParserMessages.EXPECTED_IDENTIFIER_IN_MAP_LAMBDA) ?: return null
        expect(TokenType.Arrow, ParserMessages.EXPECTED_ARROW_IN_MAP_LAMBDA) ?: return null
        val body = parseExpression(0) ?: return null
        expectSymbol(Symbols.RIGHT_PAREN, ParserMessages.EXPECTED_RIGHT_PAREN_AFTER_MAP) ?: return null
        return Expression.MapCall(
            sequence = sequence,
            parameterName = parameterToken.text,
            body = body,
            span = mergeSpan(startSpan, body.span),
        )
    }

    /**
     * Parses a reduce call expression starting after the `reduce` keyword.
     */
    private fun parseReduceCall(startSpan: SourceSpan): Expression? {
        expectSymbol(Symbols.LEFT_PAREN, ParserMessages.EXPECTED_LEFT_PAREN_AFTER_REDUCE) ?: return null
        val sequence = parseExpression(0) ?: return null
        expectSymbol(Symbols.COMMA, ParserMessages.EXPECTED_COMMA_AFTER_REDUCE_SEQUENCE) ?: return null
        val neutral = parseExpression(0) ?: return null
        expectSymbol(Symbols.COMMA, ParserMessages.EXPECTED_COMMA_AFTER_NEUTRAL) ?: return null
        val accumulatorParameterToken =
            expect(TokenType.Identifier, ParserMessages.EXPECTED_ACCUMULATOR_IDENTIFIER) ?: return null
        val elementParameterToken =
            expect(TokenType.Identifier, ParserMessages.EXPECTED_ELEMENT_IDENTIFIER) ?: return null
        expect(TokenType.Arrow, ParserMessages.EXPECTED_ARROW_IN_REDUCE_LAMBDA) ?: return null
        val body = parseExpression(0) ?: return null
        expectSymbol(Symbols.RIGHT_PAREN, ParserMessages.EXPECTED_RIGHT_PAREN_AFTER_REDUCE) ?: return null
        return Expression.ReduceCall(
            sequence = sequence,
            neutral = neutral,
            accumulatorParameter = accumulatorParameterToken.text,
            elementParameter = elementParameterToken.text,
            body = body,
            span = mergeSpan(startSpan, body.span),
        )
    }

    /**
     * Returns the precedence of a binary operator.
     */
    private fun precedence(op: String): Int =
        when (op) {
            Operators.PLUS, Operators.MINUS -> 1
            Operators.MULTIPLY, Operators.DIVIDE -> 2
            Operators.POWER -> 3
            else -> 0
        }

    /**
     * Unescapes a string literal token into its runtime representation.
     */
    private fun unescapeString(text: String): String {
        if (text.length < 2) return ""
        val raw = text.substring(1, text.length - 1)
        val sb = StringBuilder()
        var i = 0
        while (i < raw.length) {
            val ch = raw[i]
            if (ch == '\\' && i + 1 < raw.length) {
                val next = raw[i + 1]
                sb.append(
                    when (next) {
                        'n' -> '\n'
                        't' -> '\t'
                        '"' -> '"'
                        '\\' -> '\\'
                        else -> next
                    },
                )
                i += 2
            } else {
                sb.append(ch)
                i++
            }
        }
        return sb.toString()
    }

    /**
     * Advances the cursor until a new statement boundary is detected.
     */
    private fun recoverToNextStatement() {
        while (!isAtEnd()) {
            if (
                current().type == TokenType.Keyword &&
                current().text in setOf(Keywords.VAR, Keywords.OUT, Keywords.PRINT)
            ) {
                return
            }
            advance()
        }
    }

    private fun match(type: TokenType): Boolean {
        if (check(type)) {
            advance()
            return true
        }
        return false
    }

    private fun matchKeyword(text: String): Boolean {
        if (check(TokenType.Keyword) && current().text == text) {
            advance()
            return true
        }
        return false
    }

    private fun matchMinus(): Boolean {
        if (check(TokenType.Operator) && current().text == Operators.MINUS) {
            advance()
            return true
        }
        return false
    }

    private fun matchSymbol(text: String): Boolean {
        if (check(TokenType.Symbol) && current().text == text) {
            advance()
            return true
        }
        return false
    }

    private fun expect(
        type: TokenType,
        message: String,
    ): Token? {
        if (check(type)) return advance()
        diagnostics += Diagnostic(message, current().span)
        return null
    }

    private fun expectSymbol(
        text: String,
        message: String,
    ): Token? {
        if (check(TokenType.Symbol) && current().text == text) return advance()
        diagnostics += Diagnostic(message, current().span)
        return null
    }

    private fun check(type: TokenType): Boolean = current().type == type

    private fun advance(): Token {
        if (!isAtEnd()) index++
        return previous()
    }

    private fun current(): Token = tokens[index]

    private fun previous(): Token = tokens[index - 1]

    private fun isAtEnd(): Boolean = current().type == TokenType.Eof
}

/**
 * Result of parsing: the AST plus any diagnostics.
 */
data class ParseResult(
    val program: Program,
    val diagnostics: List<Diagnostic>,
)
