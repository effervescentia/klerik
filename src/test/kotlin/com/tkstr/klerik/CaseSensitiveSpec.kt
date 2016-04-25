package com.tkstr.klerik

import com.tkstr.klerik.TestUtil.assertThrows
import com.tkstr.klerik.TestUtil.evalInt
import com.tkstr.klerik.core.ExpressionFunction
import org.jetbrains.spek.api.Spek
import java.math.BigDecimal
import kotlin.test.assertEquals

/**
 * CaseSensitiveSpec
 *
 * @author Ben Teichman
 */
class CaseSensitiveSpec : Spek() { init {
    given("an expression") {
        on("evaluate with variables") {
            it("should match") {
                val expression = Expression("a").setVariable("a", 20)
                assertEquals(20, evalInt(expression))
            }
            it("should not match") {
                val expression = Expression("a").setVariable("A", 20)
                assertThrows("Unknown operator or function 'a'") { evalInt(expression) }
            }
            it("should match multiple") {
                val expression = Expression("a + B")
                        .setVariable("a", 10)
                        .setVariable("B", 10)
                assertEquals(20, evalInt(expression))
            }
            it("should not match multiple") {
                val expression = Expression("A + B")
                        .setVariable("A", 10)
                        .setVariable("b", 10)
                assertThrows("Unknown operator or function 'B'") { evalInt(expression) }
            }
        }

        on("evaluate with functions") {
            val testSum = object : ExpressionFunction("testSum", -1) {
                override fun eval(params: List<Any>): BigDecimal {
                    return params.fold(null as BigDecimal?) { sum, next -> if (sum == null) next as BigDecimal else sum.add(next as BigDecimal) }!!
                }
            }

            it("should match") {
                val expression = Expression("a + testSum(1, 3)").setVariable("a", 3);
                expression.addFunction(testSum)
                assertEquals(7, evalInt(expression))
            }
            it("should not match") {
                val expression = Expression("a + tEStsum(1, 3)").setVariable("a", 3)
                expression.addFunction(testSum)
                assertThrows("Unknown operator or function 'tEStsum'") { expression.eval() }
            }
        }
    }
}
}