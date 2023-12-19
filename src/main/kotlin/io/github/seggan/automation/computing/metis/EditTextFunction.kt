package io.github.seggan.automation.computing.metis

import io.github.seggan.automation.computing.CpuTask
import io.github.seggan.automation.pluginInstance
import io.github.seggan.automation.serial.BlockPosPdt
import io.github.seggan.automation.util.WaitingFunction
import io.github.seggan.automation.util.WaitingFunctionExecutor
import io.github.seggan.automation.util.paginate
import io.github.seggan.metis.runtime.*
import io.github.seggan.metis.util.pop
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.title.Title
import org.apache.http.client.utils.URIBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEditBookEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import java.util.concurrent.ConcurrentHashMap

object EditTextFunction : WaitingFunction(Arity.ONE) {

    private val finished = ConcurrentHashMap<BlockPosition, String?>()

    init {
        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    override fun getExecutor(nargs: Int): WaitingFunctionExecutor = object : WaitingFunctionExecutor {

        lateinit var block: BlockPosition

        override fun init(state: State) {
            val text = state.stack.pop().stringValue()
            block = CpuTask.getLocationOfState(state)!!
            val location = block.toLocation()

            pluginInstance.runOnNextTick {
                val players = location.getNearbyPlayers(pluginInstance.interactionRadius)
                val nearest = players.minByOrNull { it.location.distanceSquared(location) } ?: return@runOnNextTick

                val url = URIBuilder("https://seggan.github.io/automation")
                    .addParameter("conn", nearest.address.hostName)
                    .build()
                val link = Component.text().clickEvent(ClickEvent.openUrl())
            }
        }

        override fun step(state: State): Value? {
            if (finished.containsKey(block)) {
                return finished.remove(block)?.metisValue() ?: Value.Null
            }
            return null
        }
    }
}