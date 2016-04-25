package com.tkstr.klerik.fixtures

import com.tkstr.klerik.core.ExpressionFunction
import com.tkstr.klerik.core.Util.test
import java.math.BigDecimal

/**
 * Functions
 *
 * @author Ben Teichman
 */
object Functions {
    val MAX = object : ExpressionFunction("max", -1) {
        override fun eval(params: List<Any>) = extrema(name, params) { it > 0 }
    }

    val MIN = object : ExpressionFunction("min", -1) {
        override fun eval(params: List<Any>) = extrema(this.name, params) { it < 0 };
    }

    private fun extrema(name: String, params: List<Any>, comparison: (diff: Int) -> Boolean): Any {
        return when {
            params.all { it is BigDecimal } -> {
                @Suppress("UNCHECKED_CAST")
                (params as List<BigDecimal>)
                test(params.size == 0, "'$name' requires at least one parameter")
                var result: BigDecimal? = null
                params.forEach { if (result == null || comparison(it.compareTo(result))) result = it }
                return result!!
            }
            else -> Unit
        }
    }
}