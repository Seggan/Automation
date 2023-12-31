package io.github.seggan.automation.util

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import org.bukkit.inventory.ItemStack

class ItemBuilder {

    var name: String? = null
    var material: MaterialType? = null
    var id: String? = null
        set(value) {
            field = value?.let { "AUTOMATION_$it" }
        }

    private val lore = mutableListOf<String>()

    operator fun String.unaryPlus() {
        lore += this
    }

    fun build(): SlimefunItemStack {
        checkNotNull(name) { "Name is not set" }
        checkNotNull(material) { "Material is not set" }
        checkNotNull(id) { "ID is not set" }

        return SlimefunItemStack(
            id!!,
            material!!.convert(),
            name!!,
            *lore.toTypedArray()
        )
    }
}


sealed interface MaterialType {

    fun convert(): org.bukkit.inventory.ItemStack

    class Material(private val material: org.bukkit.Material) : MaterialType {
        override fun convert() = ItemStack(material)
    }

    class ItemStack(private val itemStack: org.bukkit.inventory.ItemStack) : MaterialType {
        override fun convert() = itemStack
    }

    class Head(private val texture: String) : MaterialType {
        override fun convert() = SlimefunUtils.getCustomHead(texture)
    }
}

inline fun buildSlimefunItem(builder: ItemBuilder.() -> Unit): SlimefunItemStack {
    return ItemBuilder().apply(builder).build()
}