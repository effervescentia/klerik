package com.tkstr.klerik

import com.tkstr.klerik.TestUtil.evalBoolean
import org.jetbrains.spek.api.Spek
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * BooleanSpec
 *
 * @author Ben Teichman
 */
class BooleanSpec : Spek() { init {
    given("an logical expression") {
        on("evaluation") {
            it("should 'and' booleans") {
                assertFalse(evalBoolean("1 && 0"))
                assertTrue(evalBoolean("1 && 1"))
                assertFalse(evalBoolean("0 && 0"))
                assertFalse(evalBoolean("0 && 1"))
            }
            it("should 'or' booleans") {
                assertTrue(evalBoolean("1 || 0"))
                assertTrue(evalBoolean("1 || 1"))
                assertFalse(evalBoolean("0 || 0"))
                assertTrue(evalBoolean("0 || 1"))
            }
            it("should evaluate comparisons") {
                assertTrue(evalBoolean("2 > 1"))
                assertFalse(evalBoolean("2 < 1"))
                assertFalse(evalBoolean("1 > 2"))
                assertTrue(evalBoolean("1 < 2"))
                assertTrue(evalBoolean("1 >= 1"))
                assertTrue(evalBoolean("1.1 >= 1"))
                assertFalse(evalBoolean("1 >= 2"))
                assertTrue(evalBoolean("1 <= 1"))
                assertFalse(evalBoolean("1.1 <= 1"))
                assertTrue(evalBoolean("1 <= 2"))
                assertFalse(evalBoolean("1 == 2"))
                assertTrue(evalBoolean("1 == 1"))
                assertTrue(evalBoolean("1 != 2"))
                assertFalse(evalBoolean("1 != 1"))
            }
            it("should evaluate combined comparisons") {
                assertTrue(evalBoolean("(2 > 1) || (1 == 0)"))
                assertFalse(evalBoolean("(2 > 3) || (1 == 0)"))
                assertTrue(evalBoolean("(2 > 3) || (1 == 0) || (1 && 1)"))
            }
            it("should evaluate mixed expressions") {
                assertFalse(evalBoolean("1.5 * 7 == 3"))
                assertTrue(evalBoolean("1.5 * 7 == 10.5"))
            }
            it("should evaluate constants") {
                assertTrue(evalBoolean("true != false"))
                assertFalse(evalBoolean("true == 2"))
                assertFalse(evalBoolean("true && false"))
                assertTrue(evalBoolean("true || false"))
            }
        }
    }
}
}