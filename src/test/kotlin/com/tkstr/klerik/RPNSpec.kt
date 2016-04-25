package com.tkstr.klerik

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * RPNSpec
 *
 * @author Ben Teichman
 */
class RPNSpec : Spek() { init {
    given("an expression") {
        on("converting to RPN") {
            it("should extract operators") {
                assertRPN("1 2 +", "1+2")
                assertRPN("1 2 4 / +", "1+2/4")
                assertRPN("1 2 + 4 /", "(1+2)/4")
                assertRPN("1.9 2.8 + 4.7 /", "(1.9+2.8)/4.7")
                assertRPN("1.98 2.87 + 4.76 /", "(1.98+2.87)/4.76")
                assertRPN("3 4 2 * 1 5 - 2 3 ^ ^ / +", "3 + 4 * 2 / (1 - 5) ^ 2 ^ 3")
            }
            it("should extract functions") {
                assertRPN("( 2.36 max", "max(2.36)")
                assertRPN("( -7 8 max", "max(-7, 8)")
                assertRPN("( ( 3.7 min ( 2.6 -8.0 max max", "max(min(3.7),max(2.6,-8.0))")
            }
            it("should extract strings") {
                assertRPN("'144'", "'144'")
                assertRPN("'my name is ' 'Ben' +", "'my name is ' + 'Ben'")
                assertRPN("'my age is ' ( 24 25 min +", "'my age is ' + min(24, 25)")
                assertRPN("'my age is ' ( 24 25 min + 'yrs' +", "'my age is ' + min(24, 25) + 'yrs'")
                assertRPN("( 'a' 'b' min", "min('a', 'b')")
            }
        }
    }
}
    fun assertRPN(expected: String, expression: String) {
        assertEquals(expected, Expression(expression).toRPN())
    }
}