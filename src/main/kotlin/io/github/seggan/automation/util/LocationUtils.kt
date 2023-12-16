package io.github.seggan.automation.util

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player

val Block.position: BlockPosition
    get() = BlockPosition(this)

val Location.position: BlockPosition
    get() = BlockPosition(this)

val BlockPosition.location: Location
    get() = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

fun Location.sendNearby(message: Component) {
    val nearby = world.getNearbyEntitiesByType(
        Player::class.java,
        this,
        2.0,
        2.0,
        2.0
    )
    for (player in nearby) {
        player.sendMessage(message)
    }
}