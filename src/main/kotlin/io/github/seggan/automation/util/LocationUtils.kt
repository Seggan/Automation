package io.github.seggan.automation.util

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import org.bukkit.Location
import org.bukkit.block.Block

val Block.position: BlockPosition
    get() = BlockPosition(this)

val Location.position: BlockPosition
    get() = BlockPosition(this)

val BlockPosition.location: Location
    get() = Location(world, x.toDouble(), y.toDouble(), z.toDouble())