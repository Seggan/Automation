package io.github.seggan.automation.computing.peripherals

import io.github.seggan.automation.computing.CpuTask
import io.github.seggan.metis.runtime.Value
import io.github.seggan.metis.runtime.buildTable
import io.github.seggan.metis.runtime.convertTo
import io.github.seggan.metis.runtime.intrinsics.NativeLibrary
import io.github.seggan.metis.runtime.intrinsics.oneArgFunction
import io.github.seggan.metis.runtime.intrinsics.twoArgFunction
import io.github.seggan.metis.runtime.stringValue
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import java.util.concurrent.ConcurrentHashMap

object Peripherals : NativeLibrary("peripherals") {

    private val peripherals = ConcurrentHashMap<BlockPosition, ConcurrentHashMap<String, Value.Table>>()

    fun addPeripheral(pos: BlockPosition, peripheral: NativeLibrary) {
        peripherals.getOrPut(pos, ::ConcurrentHashMap)[peripheral.name] = buildTable(peripheral::init)
    }

    fun removePeripherals(pos: BlockPosition) {
        peripherals.remove(pos)
    }

    override fun init(lib: MutableMap<String, Value>) {
        lib["get"] = oneArgFunction { name ->
            val pos = CpuTask.getPositionOfState(this)
            peripherals[pos]?.get(name.stringValue()) ?: Value.Null
        }
        lib["register"] = twoArgFunction { name, peripheral ->
            val pos = CpuTask.getPositionOfState(this)
            peripherals.getOrPut(pos, ::ConcurrentHashMap)[name.stringValue()] = peripheral.convertTo<Value.Table>()
            Value.Null
        }
    }
}