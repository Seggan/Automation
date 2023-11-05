package io.github.seggan.automation.registries

import io.github.seggan.automation.util.MaterialType
import io.github.seggan.automation.util.buildSlimefunItem
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock

@Suppress("MemberVisibilityCanBePrivate")
object ItemRegistry {

    val IRON_CPU = buildSlimefunItem(Themes.COMPONENT) {
        name = "Iron CPU"
        id = "IRON_CPU"
        material = MaterialType.Head(Heads.IRON_CPU)    
    }

    fun register(addon: SlimefunAddon) {
        UnplaceableBlock(GroupRegistry.COMPONENTS, IRON_CPU, RecipeType.NULL, arrayOf()).register(addon)
    }
}