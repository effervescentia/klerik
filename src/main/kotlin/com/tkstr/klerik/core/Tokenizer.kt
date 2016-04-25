package com.tkstr.klerik.core

import com.tkstr.klerik.*
import com.tkstr.klerik.core.Util.charEql
import com.tkstr.klerik.core.Util.isDigitChar
import com.tkstr.klerik.core.Util.isParamsChar
import com.tkstr.klerik.core.Util.isVariableChar
import com.tkstr.klerik.core.Util.isVariablePrefix
import com.tkstr.klerik.core.Util.test

/**
 * Tokenizer
 *
 * @author Ben Teichman
 */
class Tokenizer(val exp: Expression) : Iterator<String?> {
    val input = exp.expression.trim()
    var pos = 0
    var previousToken: String? = null

    override fun hasNext() = pos < input.length

    fun peekNext() = if (pos < input.lastIndex) input[pos + 1] else '0'
    fun peekRemaining() = if (hasNext()) input.subSequence(pos, input.lastIndex) else ""

    override fun next(): String? {
        val token = StringBuilder()
        if (!hasNext()) {
            previousToken = null
            return previousToken
        }

        handleWhitespace()
        var ch = current()

        when {
            isStringBoundary(ch) -> handleString(token)
            ch.isDigit() -> handleNumber(token)
            isNegativeSign(ch) -> handleNegativeNumber(token)
            isVariablePrefix(ch) -> handleVariable(token)
            isParamsChar(ch) -> handleChar(ch, token)
            else -> handleOther(token)
        }

        previousToken = token.toString()
        return previousToken!!
    }

    private fun handleOther(token: StringBuilder) {
        var ch = current()
        while (!(isVariableChar(ch) || ch.isWhitespace() || isParamsChar(ch) || ch == APOSTROPHE) && hasNext()) {
            ch = advance(token)
            if (ch == MINUS_SIGN) break
        }
        test(!exp.isOperator(token.toString()), "Unknown operator '$token' at position ${pos - token.length + 1}")
    }

    private fun handleVariable(token: StringBuilder) {
        var ch = current()
        while (isVariableChar(ch) && hasNext()) ch = advance(token)
    }

    private fun handleNegativeNumber(token: StringBuilder) {
        handleChar(MINUS_SIGN, token)
        token.append(next())
    }

    private fun handleChar(ch: Char, token: StringBuilder) {
        token.append(ch)
        pos++
    }

    private fun handleNumber(token: StringBuilder) {
        var ch = current()
        while (isDigitChar(ch) && hasNext()) ch = advance(token)
    }

    private fun handleString(token: StringBuilder) {
        do {
            val ch = advance(token)
        } while ((ch != APOSTROPHE || token.last() == '\\') && hasNext())
        test(advance(token) == APOSTROPHE, "Invalid string boundary at position $pos")
    }

    private fun isStringBoundary(ch: Char) = ch == APOSTROPHE && pos < input.lastIndex

    private fun isNegativeSign(ch: Char): Boolean {
        return (ch == MINUS_SIGN
                && peekNext().isDigit()
                && (previousToken == null
                || charEql(previousToken!!, L_PAREN)
                || charEql(previousToken!!, COMMA)
                || exp.isOperator(previousToken!!)))
    }

    private fun handleWhitespace() {
        while (current().isWhitespace() && hasNext()) pos++
    }

    private fun advance(token: StringBuilder): Char {
        token.append(input[pos++])
        return if (pos == input.length) '0' else current()
    }

    private fun current() = input[pos]
}