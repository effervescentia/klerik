package com.tkstr.klerik

import com.tkstr.klerik.TestUtil.evalInt
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * NestedSpec
 *
 * @author Ben Teichman
 */
class NestedSpec : Spek() { init {
    given("a nested expression") {
        on("nested variables used") {
            it("should evaluate") {
                val expression = Expression("2*x + 4*z")
                        .with("x", "1")
                        .with("y", "2")
                        .with("z", "2*x + 3*y")
                assertEquals(34, evalInt(expression))
            }
            it("should replace inner expression") {
                val expression = Expression("3+a+aa+aaa")
                        .with("a", "1*x")
                        .with("aa", "2*x")
                        .with("aaa", "3*x")
                        .with("x", "2")
                assertEquals(15, evalInt(expression))
            }
        }
    }
}
}