package com.tkstr.klerik.core

/**
 * ExpressionFunction
 *
 * @author Ben Teichman
 */
abstract class ExpressionFunction(name: String, numParams: Int) : LazyFunction<Any>(name, numParams) {
    override fun lazyEval(lazyParams: List<LazyValue<Any>>) = object : LazyValue<Any> {
        override fun eval() = eval(lazyParams.map { it.eval()!! })
    }

    abstract fun eval(params: List<Any>): Any
}