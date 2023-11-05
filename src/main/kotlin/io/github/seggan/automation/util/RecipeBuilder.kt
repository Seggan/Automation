package io.github.seggan.automation.util

import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap
import org.bukkit.inventory.ItemStack

class RecipeBuilder {

    private val result = Array(9) { '\u0000' }
    private var index = 0
    private val mapping = Char2ObjectOpenHashMap<ItemStack?>()

    operator fun String.unaryMinus() {
        require(this.length == 3) { "Line must be 3 characters long" }
        check(index < 3) { "Recipe must be 3 lines long" }
        for (i in 0..2) {
            val char = this[i]
            result[index * 3 + i] = char
        }
        index++
    }

    infix fun Char.means(item: ItemStack?) {
        mapping[this] = item
    }

    fun build(): Array<ItemStack?> {
        return result.map { mapping[it] }.toTypedArray()
    }
}

inline fun buildRecipe(builder: RecipeBuilder.() -> Unit): Array<ItemStack?> {
    return RecipeBuilder().apply(builder).build()
}