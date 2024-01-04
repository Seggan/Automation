package io.github.seggan.automation.computing.peripherals

import io.github.seggan.automation.computing.CpuTask
import io.github.seggan.automation.items.PeripheralUpgrade
import io.github.seggan.automation.pluginInstance
import io.github.seggan.automation.util.*
import io.github.seggan.metis.runtime.*
import io.github.seggan.metis.runtime.intrinsics.NativeLibrary
import io.github.seggan.metis.util.pop
import org.bukkit.Location
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Marker
import java.util.*

class Sensor(private val range: Int) : NativeLibrary("sensor") {

    private val rangeSq = range * range

    override fun init(lib: MutableMap<String, Value>) {
        lib["getBlockInfo"] = object : WaitingFunction(Arity.THREE) {
            override fun getExecutor(nargs: Int) = object : WaitingFunctionExecutor {

                @Volatile
                private var result: Value? = null

                override fun init(state: State) {
                    val z = state.stack.pop().intValue()
                    val y = state.stack.pop().intValue()
                    val x = state.stack.pop().intValue()

                    val computerLocation = CpuTask.getPositionOfState(state).location
                    val location = Location(computerLocation.world, x.toDouble(), y.toDouble(), z.toDouble())

                    if (computerLocation.distanceSquared(location) > rangeSq) {
                        result = Value.Null
                        return
                    }

                    pluginInstance.runOnNextTick {
                        val block = location.block
                        result = buildTable { table ->
                            table["x"] = block.x.metisValue()
                            table["y"] = block.y.metisValue()
                            table["z"] = block.z.metisValue()
                            table["type"] = block.type.name.lowercase().metisValue()
                            table["biome"] = block.biome.name.lowercase().metisValue()

                            val data = block.blockData
                            if (data is Powerable) {
                                table["powered"] = data.isPowered.metisValue()
                            }
                            if (data is Waterlogged) {
                                table["waterlogged"] = data.isWaterlogged.metisValue()
                            }
                            if (data is Ageable) {
                                table["age"] = data.age.metisValue()
                            }
                            if (data is Directional) {
                                table["facing"] = data.facing.name.lowercase().metisValue()
                            }
                        }
                    }
                }

                override fun step(state: State): Value? = result
            }
        }
        lib["getEntityInfo"] = object : WaitingFunction(Arity.ONE) {
            override fun getExecutor(nargs: Int) = object : WaitingFunctionExecutor {

                @Volatile
                private var result: Value? = null

                override fun init(state: State) {
                    val uuid = UUID.fromString(state.stack.pop().stringValue())

                    val computerLocation = CpuTask.getPositionOfState(state).location

                    pluginInstance.runOnNextTick {
                        result =
                            entityProperties(computerLocation.world.getEntity(uuid), computerLocation) ?: Value.Null
                    }
                }

                override fun step(state: State): Value? = result
            }
        }
        lib["getEntitiesInRange"] = object : WaitingFunction(Arity.ONE) {
            override fun getExecutor(nargs: Int) = object : WaitingFunctionExecutor {

                @Volatile
                private var result: Value? = null

                override fun init(state: State) {
                    val rangeValue = state.stack.pop()
                    val givenRange = if (rangeValue is Value.Null) range else rangeValue.intValue()

                    if (givenRange <= 0) {
                        result = Value.List()
                        return
                    }

                    val usedRange = givenRange.coerceAtMost(range).toDouble()
                    val computerLocation = CpuTask.getPositionOfState(state).location

                    pluginInstance.runOnNextTick {
                        result =
                            computerLocation.world.getNearbyEntities(computerLocation, usedRange, usedRange, usedRange)
                                .mapNotNull { entityProperties(it, computerLocation) }
                                .metisValue()
                    }
                }

                override fun step(state: State): Value? = result
            }
        }
    }

    private fun entityProperties(entity: Entity?, computerLocation: Location): Value? {
        if (
            entity == null
            || entity.location.distanceSquared(computerLocation) > rangeSq
            || entity is Marker
            || (entity is LivingEntity && entity.isInvisible)
        ) {
            return null
        }
        return entity.metisProperties(PeripheralUpgrade.INVENTORY_SCANNER in CpuTask.getUpgrades(computerLocation.position))
    }
}