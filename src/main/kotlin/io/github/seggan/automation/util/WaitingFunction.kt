package io.github.seggan.automation.util

import io.github.seggan.metis.runtime.*
import io.github.seggan.metis.runtime.chunk.StepResult
import io.github.seggan.metis.runtime.intrinsics.oneArgFunction
import io.github.seggan.metis.util.MutableLazy
import io.github.seggan.metis.util.push

/**
 * A function that supports yielding if it is not finished.
 */
abstract class WaitingFunction(final override val arity: Arity) : CallableValue {

    override var metatable: Value.Table? by MutableLazy {
        Value.Table(mutableMapOf("__str__".metisValue() to oneArgFunction { "<function>".metisValue() }))
    }

    final override fun call(nargs: Int): CallableValue.Executor = object : CallableValue.Executor {

        val executor = getExecutor(nargs)
        var initialized = false

        override fun step(state: State): StepResult {
            if (!initialized) {
                executor.init(state)
                initialized = true
            }
            val result = executor.step(state)
            if (result != null) {
                state.stack.push(result)
                return StepResult.FINISHED
            }
            return StepResult.YIELDED
        }
    }

    abstract fun getExecutor(nargs: Int): WaitingFunctionExecutor
}

/**
 * An executor for a [WaitingFunction].
 */
fun interface WaitingFunctionExecutor {

    /**
     * Initialize the executor.
     *
     * @param state The state to initialize with.
     */
    fun init(state: State) {}

    /**
     * Step the executor.
     *
     * @param state The state to step with.
     * @return The value to return or null if the function is not finished, thereby yielding.
     */
    fun step(state: State): Value?
}