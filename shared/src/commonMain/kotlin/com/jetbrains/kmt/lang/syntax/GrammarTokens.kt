package com.jetbrains.kmt.lang.syntax

/**
 * Reserved keywords in the language.
 */
object Keywords {
    const val VAR = "var"
    const val OUT = "out"
    const val PRINT = "print"
    const val MAP = "map"
    const val REDUCE = "reduce"
}

/**
 * Single- and multi-character symbols used by the grammar.
 */
object Symbols {
    const val LEFT_PAREN = "("
    const val RIGHT_PAREN = ")"
    const val LEFT_BRACE = "{"
    const val RIGHT_BRACE = "}"
    const val COMMA = ","
    const val EQUALS = "="
}

/**
 * Standardized parser diagnostic messages.
 */
object ParserMessages {
    const val EXPECTED_STATEMENT = "Expected statement"
    const val EXPECTED_EXPRESSION = "Expected expression"
    const val EXPECTED_IDENTIFIER_AFTER_VAR = "Expected identifier after 'var'"
    const val EXPECTED_EQUALS_AFTER_VAR = "Expected '=' after variable name"
    const val EXPECTED_STRING_AFTER_PRINT = "Expected string literal after 'print'"
    const val EXPECTED_RIGHT_PAREN_AFTER_EXPRESSION = "Expected ')' after expression"
    const val EXPECTED_COMMA_IN_SEQUENCE = "Expected ',' in sequence literal"
    const val EXPECTED_RIGHT_BRACE_AFTER_SEQUENCE = "Expected '}' after sequence literal"
    const val EXPECTED_LEFT_PAREN_AFTER_MAP = "Expected '(' after map"
    const val EXPECTED_COMMA_AFTER_MAP_SEQUENCE = "Expected ',' after map sequence"
    const val EXPECTED_IDENTIFIER_IN_MAP_LAMBDA = "Expected identifier in map lambda"
    const val EXPECTED_ARROW_IN_MAP_LAMBDA = "Expected '->' in map lambda"
    const val EXPECTED_RIGHT_PAREN_AFTER_MAP = "Expected ')' after map"
    const val EXPECTED_LEFT_PAREN_AFTER_REDUCE = "Expected '(' after reduce"
    const val EXPECTED_COMMA_AFTER_REDUCE_SEQUENCE = "Expected ',' after reduce sequence"
    const val EXPECTED_COMMA_AFTER_NEUTRAL = "Expected ',' after neutral element"
    const val EXPECTED_ACCUMULATOR_IDENTIFIER = "Expected accumulator identifier in reduce lambda"
    const val EXPECTED_ELEMENT_IDENTIFIER = "Expected element identifier in reduce lambda"
    const val EXPECTED_ARROW_IN_REDUCE_LAMBDA = "Expected '->' in reduce lambda"
    const val EXPECTED_RIGHT_PAREN_AFTER_REDUCE = "Expected ')' after reduce"
}
