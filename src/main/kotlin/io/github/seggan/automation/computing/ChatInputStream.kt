package io.github.seggan.automation.computing

import io.github.seggan.automation.pluginInstance
import io.github.seggan.automation.util.WaitingFunction
import io.github.seggan.automation.util.WaitingFunctionExecutor
import io.github.seggan.metis.runtime.*
import io.github.seggan.metis.runtime.chunk.StepResult
import io.github.seggan.metis.runtime.intrinsics.NativeObjects
import io.github.seggan.metis.runtime.intrinsics.oneArgFunction
import io.github.seggan.metis.runtime.intrinsics.twoArgFunction
import io.github.seggan.metis.util.MutableLazy
import io.github.seggan.metis.util.pop
import io.github.seggan.metis.util.push
import io.papermc.paper.event.player.AsyncChatEvent
import it.unimi.dsi.fastutil.bytes.ByteArrayPriorityQueue
import it.unimi.dsi.fastutil.bytes.BytePriorityQueues
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue
import it.unimi.dsi.fastutil.ints.IntArrayPriorityQueue
import it.unimi.dsi.fastutil.ints.IntPriorityQueues
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.InputStream

class ChatInputStream(private val location: Location) : InputStream(), Listener {

    companion object {

        private val RADIUS = pluginInstance.interactionRadius * pluginInstance.interactionRadius

        init {
            NativeObjects.INPUT_STREAM["read"] = object : WaitingFunction(Arity.TWO.requiresSelf()) {
                override fun getExecutor(nargs: Int): WaitingFunctionExecutor = object : WaitingFunctionExecutor {

                    lateinit var self: InputStream
                    lateinit var buffer: Value

                    override fun init(state: State) {
                        buffer = if (nargs == 2) state.stack.pop() else Value.Null
                        self = state.stack.pop().asObj()
                    }

                    override fun step(state: State): Value? {
                        if (self.available() == -1) return null
                        return if (buffer == Value.Null) {
                            // Read a single byte
                            self.read().toDouble().metisValue()
                        } else {
                            // Read into a buffer
                            val toBeRead = buffer.bytesValue()
                            val read = self.read(toBeRead)
                            if (read == -1) {
                                Value.Null
                            } else {
                                read.toDouble().metisValue()
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    private val buffer = IntPriorityQueues.synchronize(IntArrayFIFOQueue())

    override fun read(): Int {
        while (buffer.isEmpty) {
            Thread.sleep(1)
        }
        return buffer.dequeueInt()
    }

    // Cursed impl just for Metis purposes, do not rely on this
    override fun available(): Int = if (buffer.isEmpty) -1 else super.available()

    @EventHandler
    fun onChat(e: AsyncChatEvent) {
        if (location.distanceSquared(e.player.location) > RADIUS) return
        val msg = PlainTextComponentSerializer.plainText().serialize(e.message())
        for (b in msg) {
            buffer.enqueue(b.code)
        }
        buffer.enqueue('\n'.code)
        e.isCancelled = true
    }

    override fun close() {
        super.close()
        AsyncChatEvent.getHandlerList().unregister(this)
    }
}