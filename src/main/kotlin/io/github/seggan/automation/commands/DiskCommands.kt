package io.github.seggan.automation.commands

import io.github.seggan.automation.managers.DiskManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object CreateCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (args.size != 1) return false

        if (!sender.isOp) {
            sender.sendMessage("You must be an operator to use this command.")
            return true
        }

        val size = args.first().toLongOrNull() ?: return false
        sender.inventory.addItem(DiskManager.createDisk(size))
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> = mutableListOf()
}

object SfDosCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        val diskItem = sender.inventory.itemInMainHand
        val disk = DiskManager.getDisk(diskItem)
        if (disk == null) {
            sender.sendMessage("You must be holding a disk to use this command.")
            return true
        }

        disk.getPath("init.metis").outputStream().buffered().use { out ->
            javaClass.getResourceAsStream("/init.metis")!!.use { it.copyTo(out) }
        }

        sender.sendMessage("Disk initialized with the latest version of SF-DOS.")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String> = mutableListOf()
}