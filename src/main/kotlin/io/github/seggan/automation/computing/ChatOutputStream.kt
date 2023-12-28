package io.github.seggan.automation.computing

import io.github.seggan.automation.pluginInstance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Location
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
        val colored = MiniMessage.miniMessage().deserialize(buffer.toString())
        val component = Component.text().color(color).append(colored).build()
        buffer.clear()
        pluginInstance.runOnNextTick {
            for (player in location.getNearbyPlayers(pluginInstance.interactionRadius)) {
                player.sendMessage(component)
            }
        }
    }
}