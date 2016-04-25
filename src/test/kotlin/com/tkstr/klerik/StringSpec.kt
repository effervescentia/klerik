package com.tkstr.klerik

import com.tkstr.klerik.TestUtil.evalString
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * StringSpec
 *
 * @author Ben Teichman
 */
class StringSpec : Spek() { init {
    given("a string expression") {
        on("evaluate simple string") {
            it("should return the string value") {
                assertEquals("'simple string'", evalString("'simple string'"))
            }
        }
        on("evaluate string concatenation") {
            it("should concatenate strings") {
                assertEquals("'simple string'", evalString("'simple ' + 'string'"))
            }
            it("should concatenate complex strings") {
                assertEquals("'together we make a great string'", evalString("'together we ma' + 'ke a great string'"))
            }
            it("should concatenate escaped strings") {
                assertEquals("'together we\\'re making string great again'", evalString("'together we\\'re ma' + 'king string great again'"))
            }
            it("should only trim boundaries") {
                assertEquals("'together we\\'ll make it'", evalString("'together we\\'' + 'll make it'"))
            }
        }
        on("add string to number") {
            it("should convert number to a string") {
                assertEquals("'I am 24'", evalString("'I am ' + 24"))
            }
            it("should concatenate multiple numbers into the string") {
                assertEquals("'I\\'m visiting on the 141516'", evalString("'I\\'m visiting on the ' + 14 + 15 + 16"))
                assertEquals("'I\\'m visiting on the 45'", evalString("'I\\'m visiting on the ' + (14 + 15 + 16)"))
                assertEquals("'I\\'m visiting on the 141548'", evalString("'I\\'m visiting on the ' + 14 + 15 + 16 * 3"))
                assertEquals("'I\\'m visiting on the 135'", evalString("'I\\'m visiting on the ' + (14 + 15 + 16) * 3"))
                assertEquals("'I\\'m visiting on the 45I\\'m visiting on the 45I\\'m visiting on the 45'", evalString("('I\\'m visiting on the ' + (14 + 15 + 16)) * 3"))
            }
        }
        on("evaluate string multiplication") {
            it("should multiply simple string") {
                assertEquals("'mmm'", evalString("'m' * 3"))
            }
            it("should multiply despite order") {
                assertEquals("'mmm'", evalString("3 * 'm'"))
            }
            it("should multiply longer string") {
                assertEquals("'ho ho ho ! it\\'s santa!'", evalString("'ho ' * 3 + '! it\\'s santa!'"))
            }
            it("should multiply string with numbers") {
                assertEquals("'dddd'", evalString("1 * 'd' * 2 * 2"))
            }
        }
    }
}
}