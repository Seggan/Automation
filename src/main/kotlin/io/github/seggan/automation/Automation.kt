package io.github.seggan.automation

import io.github.seggan.automation.registries.ItemRegistry
import io.github.seggan.automation.software.fs.AutomationFSP
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path
import kotlin.io.path.*

class Automation : AbstractAddon() {

    lateinit var diskDir: Path
        private set

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        runLater {
            log(
                """
                ################# Automation $pluginVersion #################
                
                Automation is open-source, you can contribute or report issues here:
                $bugTrackerURL
                Join the Slimefun Addon Community Discord: discord.gg/SqD3gg5SAU
                
                ###################################################
                """.trimIndent()
            )
        }

        ItemRegistry.register(this)

        val dir = config.getString("disk-dir") ?: error("disk-dir is not set")
        diskDir = dataFolder.toPath().resolve(dir)

        val a = AutomationFSP(1000000).newFileSystem(diskDir.resolve("a"), mutableMapOf("create" to "true"))
        val b = AutomationFSP(1000000).newFileSystem(diskDir.resolve("b"), mutableMapOf("create" to "true"))
        val link = diskDir.resolve("a/mnt/test")
        if (!link.exists()) {
            link.createSymbolicLinkPointingTo(diskDir.toAbsolutePath().resolve("b"))
            a.provider().reindex()
        }

        a.getPath("a.txt").moveTo(a.getPath("b.txt"))
        log(a.getPath("/mnt/test/b.txt").readText())
    }

    override fun onDisable() {
        instance = null
    }

    fun log(message: String) {
        for (line in message.split('\n')) {
            server.logger.info(line)
        }
    }

    fun runLater(ticks: Int = 0, action: () -> Unit) {
        server.scheduler.runTaskLater(this, action, ticks.toLong())
    }

    fun key(key: String) = NamespacedKey(this, key)

    override fun getJavaPlugin(): JavaPlugin = this
    override fun getBugTrackerURL(): String = "https://github.com/Seggan/Automation/issues"
}

private var instance: Automation? = null

val pluginInstance: Automation
    get() = instance ?: error("Plugin is not enabled")