package io.github.seggan.automation.computing.metis

import io.github.seggan.metis.runtime.State
import io.github.seggan.metis.runtime.Value
import io.github.seggan.metis.runtime.get
import io.github.seggan.metis.runtime.set

fun State.preinit() {
    val io = globals["io"] as Value.Table
    io["editText"] = EditTextFunction
}