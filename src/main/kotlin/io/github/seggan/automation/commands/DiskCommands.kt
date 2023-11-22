package io.github.seggan.automation.commands

import io.github.seggan.automation.managers.DiskManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object CreateCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (args.size != 1) return false

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