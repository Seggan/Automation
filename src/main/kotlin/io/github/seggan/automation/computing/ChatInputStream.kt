package io.github.seggan.automation.computing

import io.github.seggan.automation.pluginInstance
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
        const val RADIUS = 3.0

        init {
            NativeObjects.INPUT_STREAM["read"] = object : CallableValue {

                override var metatable: Value.Table? by MutableLazy {
                    Value.Table(mutableMapOf("__str__".metisValue() to oneArgFunction { "<function>".metisValue() }))
                }

                override val arity = Arity.TWO.requiresSelf()

                override fun call(nargs: Int): CallableValue.Executor = object : CallableValue.Executor {

                    lateinit var self: InputStream
                    lateinit var buffer: Value

                    override fun step(state: State): StepResult {
                        if (!::buffer.isInitialized) {
                            buffer = if (nargs == 2) state.stack.pop() else Value.Null
                        }
                        if (!::self.isInitialized) {
                            self = state.stack.pop().asObj()
                        }
                        if (self.available() == -1) return StepResult.YIELDED
                        val result = if (buffer == Value.Null) {
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
                        state.stack.push(result)
                        return StepResult.FINISHED
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
        if (location.distanceSquared(e.player.location) > RADIUS * RADIUS) return
        val msg = PlainTextComponentSerializer.plainText().serialize(e.message())
        for (b in msg.toByteArray()) {
            buffer.enqueue(b.toInt())
        }
        buffer.enqueue('\n'.code)
        e.isCancelled = true
    }

    override fun close() {
        super.close()
        AsyncChatEvent.getHandlerList().unregister(this)
    }
}