package com.tkstr.klerik.core

import java.math.BigDecimal

/**
 * Laziness
 *
 * @author Ben Teichman
 */
interface LazyValue<T> {
    fun eval(): T?
}

interface LazyNumber : LazyValue<BigDecimal> {}
interface LazyString : LazyValue<StringBuilder> {}

abstract class LazyFunction<T>(val name: String, val numParams: Int) {
    fun numParamsVaries(): Boolean = numParams < 0

    abstract fun lazyEval(lazyParams: List<LazyValue<T>>): LazyValue<T>
}