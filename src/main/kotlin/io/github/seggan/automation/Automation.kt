package io.github.seggan.automation

import io.github.seggan.automation.commands.SuperCommand
import io.github.seggan.automation.registries.Items
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path

class Automation : AbstractAddon() {

    lateinit var diskDir: Path
        private set

    override fun onEnable() {
        instance = this

        config.options().copyDefaults(true)
        saveConfig()

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

        val mainCommand = getCommand("automation") ?: error("Failed to get command")
        mainCommand.setExecutor(SuperCommand.MAIN)
        mainCommand.tabCompleter = SuperCommand.MAIN

        Items.register(this)

        val dir = config.getString("disks.dir") ?: error("disks.dir is not set")
        diskDir = dataFolder.toPath().resolve(dir)
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