package com.tkstr.klerik

import com.tkstr.klerik.TestUtil.assertThrows
import com.tkstr.klerik.TestUtil.evalDouble
import com.tkstr.klerik.TestUtil.evalInt
import org.jetbrains.spek.api.Spek
import java.math.RoundingMode
import kotlin.test.assertEquals

/**
 * EvalSpec
 *
 * @author Ben Teichman
 */
class EvalSpec : Spek() { init {
    given("an expression") {
        on("evaluating an invalid expression") {
            it("should not allow consecutive numbers") {
                assertExpressionThrows("Too many numbers or variables", "12 8 2")
            }
            it("should not allow consecutive expressions") {
                assertExpressionThrows("Too many numbers or variables", "(12)(18)")
            }
            it("should not allow consecutive operators") {
                assertExpressionThrows("Too many operators or functions at '+'", "12+ *18")
            }
            it("should not allow an empty expression") {
                assertExpressionThrows("Empty expression", "")
            }
            it("should not allow invalid bracket placement") {
                assertExpressionThrows("Missing operator at character position 4", "2*3(5*3)")
            }
            it("should not allow invalid nested bracket placement") {
                assertExpressionThrows("Missing operator at character position 5", "2*(3((5*3)))")
            }
        }
        on("evaluating expressions with brackets") {
            it("should return the correct result") {
                assertEquals(3, evalInt("(1+2)"))
                assertEquals(3, evalInt("((1+2))"))
                assertEquals(3, evalInt("(((1+2)))"))
                assertEquals(9, evalInt("(1+2)*(1+2)"))
                assertEquals(10, evalInt("(1+2)*(1+2)+1"))
                assertEquals(12, evalInt("(1+2)*((1+2)+1)"))
            }
        }
        on("evaluating invalid operators") {
            it("should not allow unknown operators") {
                assertExpressionThrows("Unknown operator '#' at position 2", "7#9")
            }
            it("should not allow multi-symbol unknown operators") {
                assertExpressionThrows("Unknown operator '#' at position 13", "123.6*-9.8-7#9")
            }
        }
        on("evaluating arithmetic") {
            it("should follow precedence") {
                assertEquals(3, evalInt("1+2"))
                assertEquals(2, evalInt("4/2"))
                assertEquals(5, evalInt("3+4/2"))
                assertEquals(3.5, evalDouble("(3+4)/2"))
                assertEquals(7.98, evalDouble("4.2*1.9"))
                assertEquals(2, evalInt("8%3"))
                assertEquals(0, evalInt("8%2"))
            }
            it("should compute powers") {
                assertEquals(16, evalInt("2^4"))
                assertEquals(256, evalInt("2^8"))
                assertEquals(9, evalInt("3^2"))
                assertEquals(6.25, evalDouble("2.5^2"))
                assertEquals(28.34045, evalDouble("2.6^3.5"))
            }
        }
        on("evaluating functions") {
            it("should compute 'min'") {
                assertEquals(3.78787, evalDouble("min(3.78787, 3.83838)"))
                assertEquals(3.78787, evalDouble("min(4.13223, 3.78787)"))
            }
            it("should compute 'max'") {
                assertEquals(3.78787, evalDouble("max(3.78787, 3.23412)"))
                assertEquals(3.78787, evalDouble("max(3.23412, 3.78787)"))
            }
        }
        on("set math context") {
            it("should accept new precision") {
                assertEquals(0.83, evalDouble(Expression("2.5/3").setPrecision(2)))
                assertEquals(0.833, evalDouble(Expression("2.5/3").setPrecision(3)))
                assertEquals(0.83333333, evalDouble(Expression("2.5/3").setPrecision(8)))
            }
            it("should accept new rounding mode") {
                assertEquals(0.8333333, evalDouble(Expression("2.5/3").setRoundingMode(RoundingMode.DOWN)))
                assertEquals(0.8333334, evalDouble(Expression("2.5/3").setRoundingMode(RoundingMode.UP)))
            }
        }
    }
}

    fun assertExpressionThrows(message: String, expression: String) {
        assertThrows(message) { Expression(expression).eval() }
    }

}