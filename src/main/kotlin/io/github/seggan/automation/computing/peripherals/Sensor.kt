package io.github.seggan.automation.computing.peripherals

import io.github.seggan.automation.computing.CpuTask
import io.github.seggan.automation.pluginInstance
import io.github.seggan.automation.util.WaitingFunction
import io.github.seggan.automation.util.WaitingFunctionExecutor
import io.github.seggan.automation.util.location
import io.github.seggan.automation.util.text
import io.github.seggan.metis.runtime.*
import io.github.seggan.metis.runtime.intrinsics.NativeLibrary
import io.github.seggan.metis.util.pop
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.*
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
        return buildTable { table ->
            table["x"] = entity.location.x.metisValue()
            table["y"] = entity.location.y.metisValue()
            table["z"] = entity.location.z.metisValue()

            table["type"] = entity.type.name.lowercase().metisValue()
            table["uuid"] = entity.uniqueId.toString().metisValue()

            val customName = entity.customName()
            if (customName != null) {
                table["customName"] = customName.text.metisValue()
            }

            if (entity is LivingEntity) {
                table["health"] = entity.health.metisValue()
                table["maxHealth"] = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value.metisValue()
                table["arrowsInBody"] = entity.arrowsInBody.metisValue()
                table["potionEffects"] = buildTable { effects ->
                    entity.activePotionEffects.forEach { effect ->
                        effects[effect.type.name.lowercase()] = effect.amplifier.metisValue()
                    }
                }
                table["isLeashed"] = entity.isLeashed.metisValue()
                table["isGlowing"] = entity.isGlowing.metisValue()
                table["isSneaking"] = entity.isSneaking.metisValue()
                table["isSwimming"] = entity.isSwimming.metisValue()
                table["isSleeping"] = entity.isSleeping.metisValue()
                table["isClimbing"] = entity.isClimbing.metisValue()
                table["isGliding"] = entity.isGliding.metisValue()
                table["isRiptiding"] = entity.isRiptiding.metisValue()
                table["isOnFire"] = Value.Boolean.of(entity.fireTicks > 0)
                table["isInWater"] = entity.isInWater.metisValue()
                table["isInLava"] = entity.isInLava.metisValue()
                table["isInBubbleColumn"] = entity.isInBubbleColumn.metisValue()
                table["isInRain"] = entity.isInRain.metisValue()
            }
            if (entity is org.bukkit.entity.Ageable) {
                table["isAdult"] = entity.isAdult.metisValue()
                table["age"] = entity.age.metisValue()
            }
            if (entity is Tameable) {
                table["isTamed"] = entity.isTamed.metisValue()
                val owner = entity.owner
                if (owner != null) {
                    table["owner"] = owner.uniqueId.toString().metisValue()
                }
            }
            if (entity is Breedable) {
                table["canBreed"] = entity.canBreed().metisValue()
            }
            if (entity is Sittable) {
                table["isSitting"] = entity.isSitting.metisValue()
            }
            if (entity is Player) {
                table["name"] = entity.name.metisValue()
                table["isOp"] = entity.isOp.metisValue()
                table["isFlying"] = entity.isFlying.metisValue()
                table["isSprinting"] = entity.isSprinting.metisValue()
            }
        }
    }
}