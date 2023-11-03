package io.github.seggan.automation

import org.bukkit.plugin.java.JavaPlugin

class Automation : AbstractAddon() {

    override fun onEnable() {
        instance = this

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

    override fun getJavaPlugin(): JavaPlugin = this
    override fun getBugTrackerURL(): String = "https://github.com/Seggan/Automation/issues"
}

private var instance: Automation? = null

val pluginInstance: Automation
    get() = instance ?: error("Plugin is not enabled")