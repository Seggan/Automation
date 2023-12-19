package io.github.seggan.automation

import io.github.seggan.automation.commands.SuperCommand
import io.github.seggan.automation.registries.Items
import io.github.seggan.automation.computing.CpuTask
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import java.net.URI
import java.net.URL
import java.nio.file.Path
import kotlin.properties.Delegates

class Automation : AbstractAddon() {

    lateinit var diskDir: Path
        private set

    var interactionRadius by Delegates.notNull<Double>()
        private set

    lateinit var localIp: String
        private set

    override fun onEnable() {
        instance = this

        config.options().copyDefaults(true)
        saveConfig()

        runOnNextTick {
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

        interactionRadius = config.getDouble("interaction-radius", 3.0)

        URL("https://checkip.amazonaws.com/").openStream().bufferedReader().use {
            localIp = it.readLine()
        }

        CpuTask.shutDown = false
        Thread(CpuTask, "Automation CPUs").start()
    }

    override fun onDisable() {
        instance = null
        CpuTask.shutDown = true
    }

    fun log(message: String) {
        for (line in message.split('\n')) {
            server.logger.info(line)
        }
    }

    fun runOnNextTick(ticks: Int = 0, action: () -> Unit) {
        server.scheduler.runTaskLater(this, action, ticks.toLong())
    }

    fun key(key: String) = NamespacedKey(this, key)

    override fun getJavaPlugin(): JavaPlugin = this
    override fun getBugTrackerURL(): String = "https://github.com/Seggan/Automation/issues"
}

private var instance: Automation? = null

internal val pluginInstance: Automation
    get() = instance ?: error("Plugin is not enabled")