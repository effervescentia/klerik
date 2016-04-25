package com.tkstr.klerik.core

import com.tkstr.klerik.*

/**
 * Util
 *
 * @author Ben Teichman
 */
object Util {

    fun isVariablePrefix(ch: Char) = ch.isLetter() || ch == LODASH

    fun isVariableChar(ch: Char) = isVariablePrefix(ch) || ch.isDigit()

    fun isParamsChar(ch: Char) = ch == L_PAREN || ch == R_PAREN || ch == COMMA

    fun isDigitChar(ch: Char) = ch.isDigit() || ch == DECIMAL_SEPARATOR

    fun isNumber(token: String): Boolean {
        if (charEql(token, MINUS_SIGN) || charEql(token, PLUS_SIGN)) return false
        token.forEach { if (!isDigitSignOrDecimal(it)) return false }
        return true
    }

    fun isParenthesis(element: String) = charEql(element, L_PAREN) || charEql(element, R_PAREN)

    fun isString(token: String) = token[0] == APOSTROPHE && token.length > 1

    fun isDigitSignOrDecimal(it: Char) = it.isDigit() || it == MINUS_SIGN || it == PLUS_SIGN || it == DECIMAL_SEPARATOR

    fun inPrecedenceOrder(exp: Expression, o1: Operator, token2: String): Boolean {
        return isLeftMergeable(exp, o1, token2) || o1.precedence < exp.operators[token2]!!.precedence
    }

    private fun isLeftMergeable(exp: Expression, o1: Operator, token2: String) = o1.leftAssociative
            && o1.precedence <= exp.operators[token2]!!.precedence

    fun charEql(string: String, char: Char) = char.toString().equals(string)

    fun test(boolean: Boolean, message: String) {
        if (boolean) throw ExpressionException(message)
    }

}