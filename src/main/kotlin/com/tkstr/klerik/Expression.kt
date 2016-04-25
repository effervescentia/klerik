package com.tkstr.klerik

import com.tkstr.klerik.core.*
import com.tkstr.klerik.core.Util.charEql
import com.tkstr.klerik.core.Util.isNumber
import com.tkstr.klerik.core.Util.isString
import com.tkstr.klerik.fixtures.Functions.MAX
import com.tkstr.klerik.fixtures.Functions.MIN
import com.tkstr.klerik.fixtures.Operators.ADDITION
import com.tkstr.klerik.fixtures.Operators.AND
import com.tkstr.klerik.fixtures.Operators.DIVISION
import com.tkstr.klerik.fixtures.Operators.EQL
import com.tkstr.klerik.fixtures.Operators.GT
import com.tkstr.klerik.fixtures.Operators.GT_OR_EQL
import com.tkstr.klerik.fixtures.Operators.LT
import com.tkstr.klerik.fixtures.Operators.LT_OR_EQL
import com.tkstr.klerik.fixtures.Operators.MOD
import com.tkstr.klerik.fixtures.Operators.MULTIPLICATION
import com.tkstr.klerik.fixtures.Operators.NOT_EQL
import com.tkstr.klerik.fixtures.Operators.OR
import com.tkstr.klerik.fixtures.Operators.POWER
import com.tkstr.klerik.fixtures.Operators.SUBTRACTION
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

/**
 * Expression
 *
 * @author Ben Teichman
 */
const val DECIMAL_SEPARATOR = '.'
const val PLUS_SIGN = '+'
const val MINUS_SIGN = '-'
const val COMMA = ','
const val L_PAREN = '('
const val R_PAREN = ')'
const val LODASH = '_'
const val APOSTROPHE = '\''
const val TRUE = "true"
const val FALSE = "false"

class Expression(var expression: String, var mc: MathContext = MathContext.DECIMAL32) {

    companion object {
        val PARAMS_START = object : LazyValue<Any> {
            override fun eval(): BigDecimal? = null
        }
    }

    val operators = TreeMap<String, Operator>()
    val functions = TreeMap<String, ExpressionFunction>()
    val numericVariables = TreeMap<String, BigDecimal>()
    val stringVariables = TreeMap<String, StringBuilder>()
    var rpn: List<String>? = null

    init {
        addOperator(ADDITION(this))
        addOperator(SUBTRACTION(this))
        addOperator(MULTIPLICATION(this))
        addOperator(DIVISION(this))
        addOperator(MOD(this))
        addOperator(POWER(this))
        addOperator(AND)
        addOperator(OR)
        addOperator(GT)
        addOperator(GT_OR_EQL)
        addOperator(LT)
        addOperator(LT_OR_EQL)
        addOperator(EQL)
        addOperator(NOT_EQL)

        addFunction(MAX)
        addFunction(MIN)

        numericVariables.put(TRUE, ONE)
        numericVariables.put(FALSE, ZERO)
    }

    fun addOperator(operator: Operator) = operators.put(operator.name, operator)
    fun addFunction(function: ExpressionFunction) = functions.put(function.name, function)

    fun setVariable(variable: String, value: Any): Expression {
        when {
            value is StringBuilder -> stringVariables.put(variable, value)
            value is BigDecimal -> numericVariables.put(variable, value)
            value is String && isString(value) -> stringVariables.put(variable, StringBuilder(value))
            value is Int -> numericVariables.put(variable, BigDecimal(value))
            value is Double -> numericVariables.put(variable, BigDecimal(value))
            value is Long -> numericVariables.put(variable, BigDecimal(value))
            value is String && isNumber(value) -> numericVariables.put(variable, BigDecimal(value))
            else -> {
                expression = expression.replace(Regex("(?i)\\b$variable\\b"), "($value)")
                rpn = null
            }
        }
        return this
    }

    fun setPrecision(precision: Int): Expression {
        mc = MathContext(precision)
        return this
    }

    fun setRoundingMode(roundingMode: RoundingMode): Expression {
        mc = MathContext(mc.precision, roundingMode)
        return this
    }

    fun with(variable: String, value: Any) = setVariable(variable, value)

    fun eval() = with(Stack<LazyValue<Any>>()) {
        getRPN().forEach {
            when {
                isOperator(it) -> handleOperator(it, this)
                isVariable(it) -> handleVariable(it, this)
                isFunction(it) -> handleFunction(it, this)
                charEql(it, L_PAREN) -> this.push(PARAMS_START)
                else -> handleValue(it, this)
            }
        }
        val result = this.pop().eval()
        return@with when(result) {
            is BigDecimal -> result.stripTrailingZeros()
            else -> result
        }
    }

    private fun handleValue(name: String, stack: Stack<LazyValue<Any>>) = pushExpression(stack) {
        when {
            isNumber(name) -> BigDecimal(name, mc)
            else -> name
        }
    }

    private fun handleVariable(name: String, stack: Stack<LazyValue<Any>>) = pushExpression(stack) { numericVariables[name]!! }

    private fun handleOperator(name: String, stack: Stack<LazyValue<Any>>) {
        pushExpression(stack) { operators[name]!!.eval(v2 = stack.pop().eval()!!, v1 = stack.pop().eval()!!) }
    }

    private fun pushExpression(stack: Stack<LazyValue<Any>>, eval: () -> Any) {
        stack.push(object : LazyValue<Any> {
            override fun eval() = eval()
        })
    }

    private fun handleFunction(it: String, stack: Stack<LazyValue<Any>>) {
        val func = functions[it]!!
        val params = mutableListOf<LazyValue<Any>>()
        while (stack.isNotEmpty() && stack.peek() != PARAMS_START) params.add(0, stack.pop())
        if (stack.peek() == PARAMS_START) stack.pop()
        testParams(func, params, "Function '$it' expected ${func.numParams} parameters, got ${params.size}")
        val result = func.lazyEval(params)
        stack.push(result)
    }

    private fun testParams(func: ExpressionFunction, params: MutableList<LazyValue<Any>>, message: String) {
        if (!func.numParamsVaries() && params.size != func.numParams) throw ExpressionException(message)
    }

    private fun getRPN(): List<String> {
        if (rpn == null) {
            rpn = ShuntingYard.evaluate(this)
            ShuntingYard.validate(this, rpn!!)
        }
        return rpn!!
    }

    fun toRPN(): String {
        val result = StringBuilder()
        getRPN().forEach {
            if (result.length != 0) result.append(" ")
            result.append(it)
        }
        return result.toString()
    }

    fun isOperator(operator: String) = operators.containsKey(operator)
    fun isFunction(function: String) = functions.containsKey(function)
    fun isVariable(variable: String) = numericVariables.keys.plus(stringVariables).contains(variable)
}