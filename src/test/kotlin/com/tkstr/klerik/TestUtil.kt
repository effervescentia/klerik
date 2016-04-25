package com.tkstr.klerik

import com.tkstr.klerik.core.ExpressionException
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * TestUtil
 *
 * @author Ben Teichman
 */
object TestUtil {
    fun evalDouble(expression: String) = evalDouble(Expression(expression))
    fun evalDouble(expression: Expression) = (expression.eval() as BigDecimal).toDouble()
    fun evalInt(expression: String) = evalInt(Expression(expression))
    fun evalInt(expression: Expression) = (expression.eval() as BigDecimal).intValueExact()
    fun evalBoolean(expression: String) = evalBoolean(Expression(expression))
    fun evalBoolean(expression: Expression) = evalInt(expression) == 1
    fun evalString(expression: String) = evalString(Expression(expression))
    fun evalString(expression: Expression) = expression.eval() as String

    fun assertThrows(message: String, test: () -> Unit) {
        val e = assertFailsWith<ExpressionException>(block = test)
        assertEquals(message, e.message)
    }
}