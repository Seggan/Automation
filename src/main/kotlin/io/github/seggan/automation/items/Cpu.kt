package io.github.seggan.automation.items

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock
import org.bukkit.inventory.ItemStack

class Cpu(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    clockSpeed: Int,
    recipeType: RecipeType,
    recipe: Array<out ItemStack>
) : UnplaceableBlock(itemGroup, item, recipeType, recipe) {
    val clockInterval = (1.0 / clockSpeed * 1e9).toLong()
}