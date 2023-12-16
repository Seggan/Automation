package io.github.seggan.automation.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class SuperCommand : TabExecutor {

    private val subCommands = mutableMapOf<String, TabExecutor>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val subCommand = args.firstOrNull() ?: return false
        val executor = subCommands[subCommand] ?: return false
        return executor.onCommand(sender, command, subCommand, args.sliceArray(1..<args.size))
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): MutableList<String>? {
        if (args.size == 1) {
            return subCommands.keys.filter { it.startsWith(args.first(), true) }.toMutableList()
        }
        val subCommand = args.firstOrNull() ?: return mutableListOf()
        val executor = subCommands[subCommand] ?: return mutableListOf()
        return executor.onTabComplete(sender, command, subCommand, args.sliceArray(1..<args.size))
    }

    fun registerSubCommand(name: String, executor: TabExecutor) {
        subCommands[name] = executor
    }

    companion object {
        val MAIN = SuperCommand()
        val DISK = SuperCommand()
            .also { MAIN.registerSubCommand("disk", it) }
            .also { it.registerSubCommand("create", CreateCommand) }
            .also { it.registerSubCommand("sf-dos", SfDosCommand) }
    }
}