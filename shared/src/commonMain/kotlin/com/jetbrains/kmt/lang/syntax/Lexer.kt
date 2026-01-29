package com.jetbrains.kmt.lang.syntax

import com.jetbrains.kmt.lang.diagnostics.Diagnostic
import com.jetbrains.kmt.lang.diagnostics.SourceSpan

/**
 * Converts a source string into a stream of [Token]s and diagnostics.
 */
class Lexer(private val source: String) {
    private val diagnostics = mutableListOf<Diagnostic>()
    private val tokens = mutableListOf<Token>()

    private var index = 0
    private var line = 1
    private var column = 1

    /**
     * Performs a full lexical scan of the input source.
     */
    fun lex(): LexerResult {
        while (!isAtEnd()) {
            val start = index
            val startLine = line
            val startColumn = column
            val char = advance()
            when {
                char.isWhitespace() -> {
                    // consume contiguous whitespace already in advance()
                }

                char.isLetter() || char == UNDERSCORE_CHAR -> {
                    while (peek().isLetterOrDigit() || peek() == UNDERSCORE_CHAR) advance()
                    val type =
                        if (isKeyword(start, index)) TokenType.Keyword else TokenType.Identifier
                    tokens += Token(type, spanFrom(start, startLine, startColumn), source)
                }

                char.isDigit() -> {
                    var hasDot = false
                    while (peek().isDigit() || (!hasDot && peek() == DECIMAL_POINT_CHAR)) {
                        if (peek() == DECIMAL_POINT_CHAR) hasDot = true
                        advance()
                    }
                    tokens += Token(
                        TokenType.Number,
                        spanFrom(start, startLine, startColumn),
                        source
                    )
                }

                char == DOUBLE_QUOTE_CHAR -> {
                    var terminated = false
                    while (!isAtEnd()) {
                        val p = advance()
                        if (p == ESCAPE_CHAR) {
                            if (!isAtEnd()) advance()
                            continue
                        }
                        if (p == DOUBLE_QUOTE_CHAR) {
                            terminated = true
                            break
                        }
                    }
                    if (!terminated) {
                        diagnostics += Diagnostic(
                            "Unterminated string literal",
                            spanFrom(start, startLine, startColumn)
                        )
                    }
                    tokens += Token(
                        TokenType.String,
                        spanFrom(start, startLine, startColumn),
                        source
                    )
                }

                char == MINUS_CHAR && peek() == GREATER_THAN_CHAR -> {
                    advance()
                    tokens += Token(
                        TokenType.Arrow,
                        spanFrom(start, startLine, startColumn),
                        source
                    )
                }

                char in OPERATOR_CHARS -> {
                    tokens += Token(
                        TokenType.Operator,
                        spanFrom(start, startLine, startColumn),
                        source
                    )
                }

                char in SYMBOL_CHARS -> {
                    tokens += Token(
                        TokenType.Symbol,
                        spanFrom(start, startLine, startColumn),
                        source
                    )
                }

                else -> {
                    diagnostics += Diagnostic(
                        "Unexpected character '$char'",
                        spanFrom(start, startLine, startColumn)
                    )
                }
            }
        }
        tokens += Token(TokenType.Eof, SourceSpan(index, 0, line, column), source)
        return LexerResult(tokens, diagnostics)
    }

    private fun spanFrom(
        start: Int,
        startLine: Int,
        startColumn: Int,
    ): SourceSpan {
        return SourceSpan(
            startOffset = start,
            length = index - start,
            line = startLine,
            column = startColumn
        )
    }

    private fun isAtEnd(): Boolean = index >= source.length

    private fun peek(): Char = if (isAtEnd()) NULL_CHAR else source[index]

    private fun advance(): Char {
        val char = source[index]
        index++
        if (char == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        return char
    }

    private fun isKeyword(
        start: Int,
        end: Int,
    ): Boolean {
        val length = end - start
        return when (length) {
            3 ->
                source.regionMatches(start, Keywords.VAR, 0, 3, ignoreCase = false) ||
                    source.regionMatches(start, Keywords.OUT, 0, 3, ignoreCase = false) ||
                    source.regionMatches(start, Keywords.MAP, 0, 3, ignoreCase = false)

            5 -> source.regionMatches(start, Keywords.PRINT, 0, 5, ignoreCase = false)
            6 -> source.regionMatches(start, Keywords.REDUCE, 0, 6, ignoreCase = false)
            else -> false
        }
    }
}

private const val NULL_CHAR = '\u0000'
private const val UNDERSCORE_CHAR = '_'
private const val DOUBLE_QUOTE_CHAR = '"'
private const val ESCAPE_CHAR = '\\'
private const val MINUS_CHAR = '-'
private const val GREATER_THAN_CHAR = '>'
private const val DECIMAL_POINT_CHAR = '.'
private const val PLUS_CHAR = '+'
private const val MULTIPLY_CHAR = '*'
private const val DIVIDE_CHAR = '/'
private const val POWER_CHAR = '^'
private const val LEFT_PAREN_CHAR = '('
private const val RIGHT_PAREN_CHAR = ')'
private const val LEFT_BRACE_CHAR = '{'
private const val RIGHT_BRACE_CHAR = '}'
private const val COMMA_CHAR = ','
private const val EQUALS_CHAR = '='

private val OPERATOR_CHARS = setOf(PLUS_CHAR, MINUS_CHAR, MULTIPLY_CHAR, DIVIDE_CHAR, POWER_CHAR)
private val SYMBOL_CHARS =
    setOf(
        LEFT_PAREN_CHAR,
        RIGHT_PAREN_CHAR,
        LEFT_BRACE_CHAR,
        RIGHT_BRACE_CHAR,
        COMMA_CHAR,
        EQUALS_CHAR,
    )

/**
 * The result of lexing: tokens plus any diagnostics.
 */
data class LexerResult(
    val tokens: List<Token>,
    val diagnostics: List<Diagnostic>,
)
