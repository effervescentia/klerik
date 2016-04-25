package com.tkstr.klerik

import com.tkstr.klerik.TestUtil.assertThrows
import com.tkstr.klerik.core.Tokenizer
import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * TokenizerSpec
 *
 * @author Ben Teichman
 */
class TokenizerSpec : Spek() { init {
    given("a tokenizer") {
        var tokenizer: Tokenizer;
        on("evaluate number") {
            it("should handle simple numbers") {
                tokenizer = tokenizer("1")
                assertEquals("1", tokenizer.next())
                assertFinished(tokenizer)
            }
            it("should handle negative numbers") {
                tokenizer = tokenizer("-1")
                assertEquals("-1", tokenizer.next())
                assertFinished(tokenizer)
            }
            it("should handle n-digit numbers") {
                tokenizer = tokenizer("123")
                assertEquals("123", tokenizer.next())
                assertFinished(tokenizer)
            }
            it("should handle negative n-digit numbers") {
                tokenizer = tokenizer("-123")
                assertEquals("-123", tokenizer.next())
                assertFinished(tokenizer)
            }
            it("should handle decimals") {
                tokenizer = tokenizer("123.4")
                assertEquals("123.4", tokenizer.next())
                assertFinished(tokenizer)
            }
            it("should handle negative decimals") {
                tokenizer = tokenizer("-123.456")
                assertEquals("-123.456", tokenizer.next())
                assertFinished(tokenizer)
            }
        }
        on("tokenize expression with extra spacing") {
            it("should ignore single space") {
                tokenizer = tokenizer("1 ")
                assertTrue(tokenizer.hasNext())
                assertEquals("1", tokenizer.next())
                assertFinished(tokenizer)
            }
            it("should collapse spaces") {
                tokenizer = tokenizer("     ")
                assertFinished(tokenizer)
            }
            it("should ignore multiple spaces") {
                tokenizer = tokenizer("   1       ")
                assertTrue(tokenizer.hasNext())
                assertEquals("1", tokenizer.next())
                assertFinished(tokenizer)
            }
            it("should ignore spaces within operation") {
                tokenizer = tokenizer("  1   +  2    ")
                assertEquals("1", tokenizer.next())
                assertEquals("+", tokenizer.next())
                assertTrue(tokenizer.hasNext())
                assertEquals("2", tokenizer.next())
                assertFinished(tokenizer)
            }
        }
        on("tokenize expression") {
            it("should tokenize the same despite spacing") {
                val tokens = listOf("1", "+", "2")
                assertTokens(tokens, "1+2")
                assertTokens(tokens, "1 + 2")
                assertTokens(tokens, " 1 + 2 ")
            }
            it("should tokenize complex expressions") {
                assertTokens(listOf("1", "+", "2", "-", "3", "/", "4", "*", "5"), "1+2-3/4*5")
            }
            it("should tokenize decimals") {
                val tokens = listOf("1", "+", "2.1", "-", "3.45", "/", "4.982", "*", "5.0")
                assertTokens(tokens, "1+2.1-3.45/4.982*5.0")
            }
            it("should tokenize negative numbers") {
                assertTokens(listOf("-3", "+", "4", "*", "-1"), "-3+4*-1")
            }
            it("should tokenize brackets") {
                val tokens = listOf("(", "-3", "+", "4", ")", "*", "-1", "/", "(", "7", "-", "(", "5", "*", "-8", ")", ")")
                assertTokens(tokens, "(-3+4)*-1/(7-(5*-8))")
            }
            it("should tokenize brackets with decimals") {
                assertTokens(listOf("(", "1.9", "+", "2.8", ")", "/", "4.7"), "(1.9+2.8)/4.7")
            }
            it("should tokenize function") {
                assertTokens(listOf("min", "(", "3.5", ")"), "min(3.5)")
            }
            it("should tokenize function within expression") {
                assertTokens(listOf("3", "-", "min", "(", "3.5", ")", "/", "9"), "3-min(3.5)/9")
            }
            it("should tokenize multiple parameters") {
                assertTokens(listOf("max", "(", "3.5", ",", "5.3", ")"), "max(3.5,5.3)")
            }
            it("should tokenize multiple parameters within expression") {
                assertTokens(listOf("3", "-", "max", "(", "3.5", ",", "5.3", ")", "/", "9"), "3-max(3.5,5.3)/9")
            }
            it("should tokenize multiple negative parameters within expression") {
                assertTokens(listOf("3", "/", "max", "(", "-3.5", ",", "-5.3", ")", "/", "9"), "3/max(-3.5,-5.3)/9")
            }
        }
        on("tokenize expression with strings") {
            it("should tokenize a simple string") {
                assertTokens(listOf("'a simple string'"), "'a simple string'")
            }
            it("should tokenize a string with an apostrophe") {
                assertTokens(listOf("'an encoded \\' string'"), "'an encoded \\' string'")
            }
            it("should tokenize a string with multiples escapes") {
                assertTokens(listOf("'an encoded \\\\' string'"), "'an encoded \\\\' string'")
            }
            it("should tokenize string operations") {
                assertTokens(listOf("'first string'", "+", "'second string'"), "'first string' + 'second string'")
            }
        }
        on("tokenizing invalid strings") {
            it("should not allow a trailing apostrophe") {
                assertThrows("Invalid string boundary at position 12") {
                    tokenizer("'the string''").next()
                }
            }
            it("should not allow multiple trailing apostrophes") {
                assertThrows("Invalid string boundary at position 12") {
                    tokenizer("'the string''''").next()
                }
            }
            it("should not allow a leading apostrophe") {
                assertThrows("Unknown operator '' at position 13") {
                    assertTokens(listOf("''", "the", "string", "'"), "''the string'")
                }
            }
        }
    }
}

    fun tokenizer(expression: String) = Tokenizer(Expression(expression))

    fun assertFinished(tokenizer: Tokenizer) {
        assertFalse(tokenizer.hasNext(), "unexpected tokens remaining in '${tokenizer.peekRemaining()}'")
        val next = tokenizer.next()
        assertNull(next, "unexpected next token '$next'")
    }

    fun assertTokens(tokens: List<String>, expression: String) {
        val tokenizer = tokenizer(expression)
        tokens.forEach { assertEquals(it, tokenizer.next()) }
        assertFinished(tokenizer)
    }
}