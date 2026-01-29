package com.jetbrains.kmt.lang.syntax

import com.jetbrains.kmt.lang.diagnostics.SourceSpan

/**
 * Enumerates the token categories produced by the lexer.
 */
enum class TokenType {
    Identifier,
    Number,
    String,
    Keyword,
    Symbol,
    Operator,
    Arrow,
    Eof,
}

/**
 * A lexical token with its source span and lazily computed text.
 */
data class Token(
    val type: TokenType,
    val span: SourceSpan,
    private val source: String,
) {
    private var cachedText: String? = null

    /**
     * Token text computed on first access from the original source.
     */
    val text: String
        get() {
            val existing = cachedText
            if (existing != null) return existing
            val computed =
                if (span.length == 0) {
                    ""
                } else {
                    source.substring(span.startOffset, span.endOffset)
                }
            cachedText = computed
            return computed
        }
}
