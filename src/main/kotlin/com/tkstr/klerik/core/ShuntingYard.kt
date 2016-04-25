package com.tkstr.klerik.core

import com.tkstr.klerik.COMMA
import com.tkstr.klerik.Expression
import com.tkstr.klerik.L_PAREN
import com.tkstr.klerik.R_PAREN
import com.tkstr.klerik.core.Util.charEql
import com.tkstr.klerik.core.Util.inPrecedenceOrder
import com.tkstr.klerik.core.Util.isNumber
import com.tkstr.klerik.core.Util.isParenthesis
import com.tkstr.klerik.core.Util.isString
import com.tkstr.klerik.core.Util.test
import java.util.*

/**
 * ShuntingYard
 *
 * @author Ben Teichman
 */
object ShuntingYard {

    data class ShuntContext(val exp: Expression) {
        val tokenizer = Tokenizer(exp)
        val outputQueue = mutableListOf<String>()
        val stack = Stack<String>()
        var lastFunction: String? = null
        var previousToken: String? = null
    }

    fun evaluate(exp: Expression): List<String> = with(ShuntContext(exp)) {
        while (tokenizer.hasNext()) {
            val token = tokenizer.next()!!
            when {
                isValue(exp, token) -> outputQueue.add(token)
                exp.isFunction(token) -> handleFunction(this, token)
                token[0].isLetter() -> handleSymbol(this, token)
                charEql(token, COMMA) -> handleParam(this)
                exp.isOperator(token) -> handleOperator(this, token)
                charEql(token, L_PAREN) -> handleParenStart(this, token)
                charEql(token, R_PAREN) -> handleParamsEnd(this)
            }
            previousToken = token
        }

        while (stack.isNotEmpty()) {
            val element = stack.pop()
            test(isParenthesis(element), "Mismatched parentheses")
            test(!(exp.isOperator(element) || isString(element)), "Unknown operator or function '$element'")
            outputQueue.add(element)
        }

        return outputQueue
    }

    private fun isValue(exp: Expression, token: String) = isNumber(token) || exp.isVariable(token) || isString(token)

    private fun handleFunction(ctx: ShuntContext, token: String) {
        handleSymbol(ctx, token)
        ctx.lastFunction = token
    }

    private fun handleParamsEnd(ctx: ShuntContext) = with(ctx) {
        while (stack.isNotEmpty() && !charEql(stack.peek(), L_PAREN)) outputQueue.add(stack.pop())
        test(stack.isEmpty(), "Mismatched parentheses")
        stack.pop()
        if (stack.isNotEmpty() && exp.isFunction(stack.peek())) outputQueue.add(stack.pop())
    }

    private fun handleParenStart(ctx: ShuntContext, token: String) = with(ctx) {
        if (previousToken != null) {
            test(isNumber(previousToken!!), "Missing operator at character position ${tokenizer.pos}")
            if (exp.isFunction(previousToken!!)) outputQueue.add(token)
        }
        handleSymbol(this, token)
    }

    private fun handleOperator(ctx: ShuntContext, token: String) = with(ctx) {
        val op = exp.operators[token]!!
        var next = peekOrNull()
        while (next != null && exp.isOperator(next) && inPrecedenceOrder(exp, op, next)) {
            outputQueue.add(stack.pop())
            next = peekOrNull()
        }
        handleSymbol(this, token)
    }

    private fun ShuntContext.peekOrNull() = if (stack.isEmpty()) null else stack.peek()

    private fun handleSymbol(ctx: ShuntContext, token: String) {
        ctx.stack.push(token)
    }

    private fun handleParam(ctx: ShuntContext) = with(ctx) {
        while (stack.isNotEmpty() && !charEql(stack.peek(), L_PAREN)) outputQueue.add(stack.pop())
        test(stack.isEmpty(), "Parse error for function '$lastFunction'")
    }

    fun validate(exp: Expression, rpn: List<String>) = with(Stack<Int>()) {
        var counter = 0
        rpn.forEach {
            when {
                charEql(it, L_PAREN) -> {
                    if (isNotEmpty()) this[lastIndex]++
                    push(0)
                }

                isNotEmpty() -> {
                    if (exp.isFunction(it)) {
                        counter -= pop() + 1
                    } else {
                        this[lastIndex]++
                    }
                }

                exp.isOperator(it) -> counter -= 2
            }
            test(counter < 0, "Too many operators or functions at '$it'")
            counter++
        }
        test(counter > 1, "Too many numbers or variables")
        test(counter < 1, "Empty expression")
    }
}