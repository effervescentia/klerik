package com.tkstr.klerik.fixtures

import com.tkstr.klerik.Expression
import com.tkstr.klerik.core.ExpressionException
import com.tkstr.klerik.core.Operator
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO
import java.math.RoundingMode

/**
 * Operators
 *
 * @author Ben Teichman
 */
object Operators {
    fun ADDITION(expression: Expression) = object : Operator("+", 20, true) {
        override fun eval(v1: Any, v2: Any) = when {
            v1 is BigDecimal && v2 is BigDecimal -> v1.add(v2, expression.mc)
            v1 is String && v2 is String -> v1.substring(0, v1.lastIndex) + v2.substring(1)
            v1 is String && v2 is BigDecimal -> v1.substring(0, v1.lastIndex) + v2.toString() + "'"
            v1 is BigDecimal && v2 is String -> "'" + v1.toString() + v2.substring(1)
            else -> badParamsError(name, v1, v2)
        }
    }

    fun SUBTRACTION(expression: Expression) = object : Operator("-", 20, true) {
        override fun eval(v1: Any, v2: Any) = when {
            v1 is BigDecimal && v2 is BigDecimal -> v1.subtract(v2, expression.mc)
            else -> badParamsError(name, v1, v2)
        }
    }

    fun MULTIPLICATION(expression: Expression) = object : Operator("*", 30, true) {
        override fun eval(v1: Any, v2: Any) = when {
            v1 is BigDecimal && v2 is BigDecimal -> v1.multiply(v2, expression.mc)
            v1 is String && v2 is BigDecimal -> stringMultiply(v1, v2)
            v1 is BigDecimal && v2 is String -> stringMultiply(v2, v1)
            else -> badParamsError(name, v1, v2)
        }

        private fun stringMultiply(string: String, multiplier: BigDecimal): String {
            var result = ""
            var original = string
            for (i in 1..multiplier.intValueExact()) {
                result = if (result.isNotEmpty()) result.substring(0, result.lastIndex) + original.substring(1) else original
            }
            return result
        }
    }

    fun DIVISION(expression: Expression) = object : Operator("/", 30, true) {
        override fun eval(v1: Any, v2: Any) = when {
            v1 is BigDecimal && v2 is BigDecimal -> v1.divide(v2, expression.mc)
            else -> badParamsError(name, v1, v2)
        }
    }

    fun MOD(expression: Expression) = object : Operator("%", 30, true) {
        override fun eval(v1: Any, v2: Any) = when {
            v1 is BigDecimal && v2 is BigDecimal -> v1.remainder(v2, expression.mc)
            else -> badParamsError(name, v1, v2)
        }
    }

    fun POWER(expression: Expression) = object : Operator("^", 40, false) {
        override fun eval(v1: Any, v2: Any) = when {
            else -> {
                v1 as BigDecimal
                v2 as BigDecimal
                val signOf2 = v2.signum()
                val dn1 = v1.toDouble()
                val v2Pos = v2.multiply(BigDecimal(signOf2))
                val remainderOf2 = v2Pos.remainder(ONE)
                val n2IntPart = v2Pos.subtract(remainderOf2)
                val intPow = v1.pow(n2IntPart.intValueExact(), expression.mc)
                val doublePow = BigDecimal(Math.pow(dn1, remainderOf2.toDouble()))

                var result = intPow.multiply(doublePow, expression.mc)
                if (signOf2 == -1) result = ONE.divide(result, expression.mc.precision, RoundingMode.HALF_UP)
                result
            }
        }
    }

    val AND = object : Operator("&&", 4, false) {
        override fun eval(v1: Any, v2: Any) = when {
            v1 is BigDecimal && v2 is BigDecimal -> logicalResult(!(zero(v1) || zero(v2)))
            else -> badParamsError(name, v1, v2)
        }
    }

    val OR = object : Operator("||", 2, false) {
        override fun eval(v1: Any, v2: Any) = when {
            v1 is BigDecimal && v2 is BigDecimal -> logicalResult(!(zero(v1) && zero(v2)))
            else -> badParamsError(name, v1, v2)
        }
    }

    private fun zero(value: BigDecimal) = value.equals(ZERO)

    val GT = object : Operator(">", 10, false) {
        override fun eval(v1: Any, v2: Any) = logical(name, v1, v2) { it == 1 }
    }

    val GT_OR_EQL = object : Operator(">=", 10, false) {
        override fun eval(v1: Any, v2: Any) = logical(name, v1, v2) { it >= 0 }
    }

    val LT = object : Operator("<", 10, false) {
        override fun eval(v1: Any, v2: Any) = logical(name, v1, v2) { it == -1 }
    }

    val LT_OR_EQL = object : Operator("<=", 10, false) {
        override fun eval(v1: Any, v2: Any) = logical(name, v1, v2) { it <= 0 }
    }

    val EQL = object : Operator("==", 10, false) {
        override fun eval(v1: Any, v2: Any) = logical(name, v1, v2) { it == 0 }
    }

    val NOT_EQL = object : Operator("!=", 10, false) {
        override fun eval(v1: Any, v2: Any) = logical(name, v1, v2) { it != 0 }
    }

    private fun logical(name: String, v1: Any, v2: Any, constraint: (Int) -> Boolean): Any {
        return when {
            v1 is BigDecimal && v2 is BigDecimal -> logicalResult(constraint(v1.compareTo(v2)))
            else -> badParamsError(name, v1, v2)
        }
    }

    private fun logicalResult(boolean: Boolean) = if (boolean) ONE else ZERO

    private fun badParamsError(name: String, v1: Any, v2: Any) {
        throw ExpressionException("'$name' operator does not accept (v1: ${v1.javaClass.simpleName}, v2: ${v2.javaClass.simpleName})")
    }
}