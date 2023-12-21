package io.github.seggan.automation.computing.metis

import io.github.seggan.metis.runtime.*

fun State.preinit() {
    val io = globals["io"] as Value.Table
    io["editText"] = EditTextFunction

    addNativeLibrary(LibApm)
}