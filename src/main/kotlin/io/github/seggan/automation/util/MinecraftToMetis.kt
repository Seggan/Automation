@file:JvmName("MinecraftToMetis")

package io.github.seggan.automation.util

import io.github.seggan.metis.runtime.*
import io.github.seggan.metis.runtime.intrinsics.fourArgFunction
import io.github.seggan.metis.runtime.intrinsics.twoArgFunction
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.inventory.meta.Damageable
import org.bukkit.potion.PotionEffect

fun Entity.metisProperties(getInventory: Boolean = true): Value = buildTable { table ->
    table["location"] = location.metisValue()

    table["type"] = type.name.lowercase().metisValue()
    table["uuid"] = uniqueId.toString().metisValue()

    table["customName"] = customName()?.text?.metisValue() ?: Value.Null

    if (this is LivingEntity) {
        table["health"] = health.metisValue()
        table["maxHealth"] = getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value.metisValue()
        table["arrowsInBody"] = arrowsInBody.metisValue()
        table["potionEffects"] = activePotionEffects.map(PotionEffect::metisProperties).metisValue()
        table["isLeashed"] = isLeashed.metisValue()
        table["isGlowing"] = isGlowing.metisValue()
        table["isSneaking"] = isSneaking.metisValue()
        table["isSwimming"] = isSwimming.metisValue()
        table["isSleeping"] = isSleeping.metisValue()
        table["isClimbing"] = isClimbing.metisValue()
        table["isGliding"] = isGliding.metisValue()
        table["isRiptiding"] = isRiptiding.metisValue()
        table["isOnFire"] = Value.Boolean.of(fireTicks > 0)
        table["isInWater"] = isInWater.metisValue()
        table["isInLava"] = isInLava.metisValue()
        table["isInBubbleColumn"] = isInBubbleColumn.metisValue()
        table["isInRain"] = isInRain.metisValue()
    }
    if (this is Ageable) {
        table["isAdult"] = isAdult.metisValue()
        table["age"] = age.metisValue()
    }
    if (this is Axolotl) {
        table["variant"] = variant.name.lowercase().metisValue()
    }
    if (this is Bee) {
        table["anger"] = anger.metisValue()
        table["hive"] = hive?.metisValue() ?: Value.Null
    }
    if (this is Breedable) {
        table["canBreed"] = canBreed().metisValue()
    }
    if (this is Cat) {
        table["catType"] = catType.name.lowercase().metisValue()
        table["collarColor"] = collarColor.color.asRGB().metisValue()
    }
    if (this is ChestedHorse) {
        table["isCarryingChest"] = isCarryingChest.metisValue()
    }
    if (this is Enderman) {
        table["carriedBlock"] = carriedBlock?.material?.name?.lowercase()?.metisValue() ?: Value.Null
    }
    if (this is Fox) {
        table["foxType"] = foxType.name.lowercase().metisValue()
    }
    if (this is Frog) {
        table["frogType"] = variant.name.lowercase().metisValue()
    }
    if (this is Horse) {
        table["horseColor"] = color.name.lowercase().metisValue()
        table["horseStyle"] = style.name.lowercase().metisValue()
    }
    if (this is InventoryHolder && getInventory) {
        table["inventory"] = inventory.contents.filterNotNull().map(ItemStack::metisProperties).metisValue()
    }
    if (this is Item) {
        table["item"] = itemStack.metisProperties()
    }
    if (this is Llama) {
        table["llamaColor"] = color.name.lowercase().metisValue()
    }
    if (this is Parrot) {
        table["parrotType"] = variant.name.lowercase().metisValue()
    }
    if (this is Pig) {
        table["isSaddled"] = hasSaddle().metisValue()
    }
    if (this is Player) {
        table["name"] = name.metisValue()
        table["isOp"] = isOp.metisValue()
        table["isFlying"] = isFlying.metisValue()
        table["isSprinting"] = isSprinting.metisValue()
    }
    if (this is Rabbit) {
        table["rabbitType"] = rabbitType.name.lowercase().metisValue()
    }
    if (this is Sheep) {
        table["sheepColor"] = color?.color?.asRGB()?.metisValue() ?: Value.Null
    }
    if (this is Tameable) {
        table["isTamed"] = isTamed.metisValue()
        val owner = owner
        if (owner != null) {
            table["owner"] = owner.uniqueId.toString().metisValue()
        }
    }
    if (this is TropicalFish) {
        table["pattern"] = pattern.name.lowercase().metisValue()
        table["bodyColor"] = bodyColor.color.asRGB().metisValue()
        table["patternColor"] = patternColor.color.asRGB().metisValue()
    }
    if (this is Villager) {
        table["profession"] = profession.name.lowercase().metisValue()
        table["villagerType"] = villagerType.name.lowercase().metisValue()
    }
    if (this is Wolf) {
        table["collarColor"] = collarColor.color.asRGB().metisValue()
    }
    if (this is Zombie) {
        table["isBaby"] = isBaby.metisValue()
    }
}

fun ItemStack.metisProperties(): Value = buildTable { table ->
    table["type"] = type.name.lowercase().metisValue()
    table["amount"] = amount.metisValue()
    table["maxStackSize"] = maxStackSize.metisValue()

    val meta = itemMeta
    if (meta != null) {
        table["displayName"] = meta.displayName()?.text?.metisValue() ?: Value.Null
        table["lore"] = (meta.lore() ?: emptyList()).map { it.text.metisValue() }.metisValue()
    }
    if (meta is AxolotlBucketMeta) {
        table["variant"] = if (meta.hasVariant()) meta.variant.name.lowercase().metisValue() else Value.Null
    }
    if (meta is BannerMeta) {
        table["patterns"] = meta.patterns.map { pattern ->
            buildTable { patternTable ->
                patternTable["color"] = pattern.color.name.lowercase().metisValue()
                patternTable["pattern"] = pattern.pattern.name.lowercase().metisValue()
            }
        }.metisValue()
    }
    if (meta is BookMeta) {
        table["author"] = meta.author?.metisValue() ?: Value.Null
        table["title"] = meta.title?.metisValue() ?: Value.Null
        table["pages"] = meta.pages().map { it.text.metisValue() }.metisValue()
    }
    if (meta is BundleMeta) {
        table["items"] = meta.items.map(ItemStack::metisProperties).metisValue()
    }
    if (meta is CompassMeta) {
        table["lodestone"] = meta.lodestone?.metisValue() ?: Value.Null
    }
    if (meta is CrossbowMeta) {
        table["chargedProjectiles"] = meta.chargedProjectiles.map(ItemStack::metisProperties).metisValue()
    }
    if (meta is Damageable) {
        table["damage"] = meta.damage.metisValue()
    }
    if (meta is EnchantmentStorageMeta) {
        table["storedEnchants"] = meta.storedEnchants.map { (enchant, level) ->
            buildTable { enchantTable ->
                enchantTable["enchant"] = enchant.key.key.lowercase().metisValue()
                enchantTable["level"] = level.metisValue()
            }
        }.metisValue()
    }
    if (meta is FireworkMeta) {
        table["power"] = meta.power.metisValue()
        table["effects"] = meta.effects.map { effect ->
            buildTable { effectTable ->
                effectTable["type"] = effect.type.name.lowercase().metisValue()
                effectTable["hasTrail"] = effect.hasTrail().metisValue()
                effectTable["hasFlicker"] = effect.hasFlicker().metisValue()
                effectTable["colors"] = effect.colors.map { it.asRGB().metisValue() }.metisValue()
                effectTable["fadeColors"] = effect.fadeColors.map { it.asRGB().metisValue() }.metisValue()
            }
        }.metisValue()
    }
    if (meta is LeatherArmorMeta) {
        table["color"] = meta.color.asRGB().metisValue()
    }
    if (meta is PotionMeta) {
        table["basePotionData"] = buildTable { basePotionDataTable ->
            basePotionDataTable["type"] = meta.basePotionData.type.name.lowercase().metisValue()
            basePotionDataTable["isExtended"] = meta.basePotionData.isExtended.metisValue()
            basePotionDataTable["isUpgraded"] = meta.basePotionData.isUpgraded.metisValue()
        }
        table["customEffects"] = meta.customEffects.map(PotionEffect::metisProperties).metisValue()
    }
    if (meta is SkullMeta) {
        table["owner"] = meta.owningPlayer?.name?.metisValue() ?: Value.Null
    }
    if (meta is SuspiciousStewMeta) {
        table["effects"] = meta.customEffects.map(PotionEffect::metisProperties).metisValue()
    }
    if (meta is TropicalFishBucketMeta) {
        table["pattern"] = meta.pattern.name.lowercase().metisValue()
        table["bodyColor"] = meta.bodyColor.color.asRGB().metisValue()
        table["patternColor"] = meta.patternColor.color.asRGB().metisValue()
    }
}

private val locationMetatable: Value.Table by lazy {
    buildTable { table ->
        fun MutableMap<Value, Value>.getCoord(coord: String): Double {
            return get(coord)?.doubleValue() ?: throw MetisRuntimeException("ValueError", "Invalid coordinate: $coord")
        }

        table["add"] = fourArgFunction(true) { selfv, xv, yv, zv ->
            val self = selfv.tableValue()
            val x = xv.doubleValue()
            val y = yv.doubleValue()
            val z = zv.doubleValue()
            self["x"] = (self.getCoord("x") + x).metisValue()
            self["y"] = (self.getCoord("y") + y).metisValue()
            self["z"] = (self.getCoord("z") + z).metisValue()
            selfv
        }
        table["__plus__"] = twoArgFunction(true) { selfv, otherv ->
            val self = selfv.tableValue()
            val other = otherv.tableValue()
            val x = self.getCoord("x") + other.getCoord("x")
            val y = self.getCoord("y") + other.getCoord("y")
            val z = self.getCoord("z") + other.getCoord("z")
            buildTable { table ->
                table["x"] = x.metisValue()
                table["y"] = y.metisValue()
                table["z"] = z.metisValue()
            }.also { it.metatable = locationMetatable }
        }
        table["subtract"] = fourArgFunction(true) { selfv, xv, yv, zv ->
            val self = selfv.tableValue()
            val x = xv.doubleValue()
            val y = yv.doubleValue()
            val z = zv.doubleValue()
            self["x"] = (self.getCoord("x") - x).metisValue()
            self["y"] = (self.getCoord("y") - y).metisValue()
            self["z"] = (self.getCoord("z") - z).metisValue()
            selfv
        }
        table["__minus__"] = twoArgFunction(true) { selfv, otherv ->
            val self = selfv.tableValue()
            val other = otherv.tableValue()
            val x = self.getCoord("x") - other.getCoord("x")
            val y = self.getCoord("y") - other.getCoord("y")
            val z = self.getCoord("z") - other.getCoord("z")
            buildTable { table ->
                table["x"] = x.metisValue()
                table["y"] = y.metisValue()
                table["z"] = z.metisValue()
            }.also { it.metatable = locationMetatable }
        }
        table["getFace"] = twoArgFunction(true) { selfv, facev ->
            val self = selfv.tableValue()
            val stringFace = facev.stringValue().uppercase()
            val face = BlockFace.entries.firstOrNull { it.name.equals(stringFace, true) }
                ?: throw MetisRuntimeException("ValueError", "Invalid face: $stringFace")
            val x = self.getCoord("x") + face.modX
            val y = self.getCoord("y") + face.modY
            val z = self.getCoord("z") + face.modZ
            buildTable { table ->
                table["x"] = x.metisValue()
                table["y"] = y.metisValue()
                table["z"] = z.metisValue()
            }.also { it.metatable = locationMetatable }
        }
    }
}

fun Location.metisValue(): Value = buildTable { table ->
    table["x"] = x.metisValue()
    table["y"] = y.metisValue()
    table["z"] = z.metisValue()
}.also { it.metatable = locationMetatable }

fun PotionEffect.metisProperties(): Value = buildTable { table ->
    table["type"] = type.name.lowercase().metisValue()
    table["duration"] = duration.metisValue()
    table["amplifier"] = amplifier.metisValue()
    table["isAmbient"] = isAmbient.metisValue()
    table["hasParticles"] = hasParticles().metisValue()
    table["hasIcon"] = hasIcon().metisValue()
}