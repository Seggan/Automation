package io.github.seggan.automation.computing

import io.github.seggan.automation.pluginInstance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.entity.Player
import java.io.OutputStream

class ChatOutputStream(private val location: Location, private val color: TextColor) : OutputStream() {

    private val buffer = StringBuilder()

    override fun write(b: Int) {
        if (b == '\n'.code) {
            flush()
        } else {
            buffer.append(b.toChar())
        }
    }

    override fun flush() {
        if (buffer.isEmpty()) return
        val component = Component.text(buffer.toString(), color)
        buffer.clear()
        pluginInstance.runLater {
            for (player in location.getNearbyEntitiesByType(Player::class.java, 3.0)) {
                player.sendMessage(component)
            }
        }
    }
}