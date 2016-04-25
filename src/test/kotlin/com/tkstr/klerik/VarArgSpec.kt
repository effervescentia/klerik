package com.tkstr.klerik

import com.tkstr.klerik.TestUtil.evalInt
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * VarArgSpec
 *
 * @author Ben Teichman
 */
class VarArgSpec : Spek() {init {
    given("a var arg expression") {
        on("evaluate") {
            it("should accept any number of arguments") {
                assertEquals(1, evalInt("max(1)"))
                assertEquals(8, evalInt("max(4,8)"))
                assertEquals(12, evalInt("max(12,4,8)"))
                assertEquals(32, evalInt("max(12,4,8,16,32)"))
            }
            it("should accept nested functions") {
                assertEquals(10, evalInt("max(1,2,max(3,4,5,max(9,10,3,4,5),8),7)"))
            }
            it("should accept zero") {
                assertEquals(0, evalInt("max(0)"))
                assertEquals(3, evalInt("max(0,3)"))
                assertEquals(2, evalInt("max(2,0,-3)"))
                assertEquals(0, evalInt("max(-2,0,-3)"))
                assertEquals(0, evalInt("max(0,0,0,0)"))
            }

        }
    }
}
}