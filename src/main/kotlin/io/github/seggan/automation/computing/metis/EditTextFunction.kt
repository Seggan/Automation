package io.github.seggan.automation.computing.metis

import io.github.seggan.automation.computing.CpuTask
import io.github.seggan.automation.pluginInstance
import io.github.seggan.automation.util.WaitingFunction
import io.github.seggan.automation.util.WaitingFunctionExecutor
import io.github.seggan.metis.runtime.*
import io.github.seggan.metis.util.pop
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.apache.http.client.utils.URIBuilder
import org.java_websocket.server.WebSocketServer
import java.util.concurrent.ConcurrentHashMap

object EditTextFunction : WaitingFunction(Arity.ONE) {

    private val finished = ConcurrentHashMap<BlockPosition, String?>()

    private val portRange = 30000..40000

    override fun getExecutor(nargs: Int): WaitingFunctionExecutor = object : WaitingFunctionExecutor {

        lateinit var block: BlockPosition
        lateinit var socket: WebSocketServer

        override fun init(state: State) {
            val text = state.stack.pop().stringValue()
            block = CpuTask.getPositionOfState(state)
            val location = block.toLocation()

            val port = portRange.random()
            val url = URIBuilder("https://seggan.github.io/automation")
                .addParameter("ip", pluginInstance.localIp)
                .addParameter("port", port.toString())
                .build()

            pluginInstance.runOnNextTick {
                val players = location.getNearbyPlayers(pluginInstance.interactionRadius)
                val nearest = players.minByOrNull { it.location.distanceSquared(location) } ?: return@runOnNextTick

                val link = Component.text().clickEvent(ClickEvent.openUrl(url.toURL()))
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