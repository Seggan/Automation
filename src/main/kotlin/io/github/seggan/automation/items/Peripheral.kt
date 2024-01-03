package io.github.seggan.automation.items

import io.github.seggan.metis.runtime.intrinsics.NativeLibrary
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock
import org.bukkit.inventory.ItemStack

class Peripheral(
    group: ItemGroup,
    item: SlimefunItemStack,
    val lib: NativeLibrary,
    recipeType: RecipeType,
    recipe: Array<out ItemStack>
) : UnplaceableBlock(group, item, recipeType, recipe)