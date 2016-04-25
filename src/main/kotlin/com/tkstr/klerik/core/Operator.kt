package com.tkstr.klerik.core

/**
 * Operator
 *
 * @author Ben Teichman
 */
abstract class Operator(val name: String, val precedence: Int, val leftAssociative: Boolean) {
    abstract fun eval(v1: Any, v2: Any): Any
}