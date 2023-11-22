package io.github.seggan.automation.registries

import io.github.seggan.automation.pluginInstance
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import org.bukkit.Material

object Groups {

    val MAIN = NestedItemGroup(
        pluginInstance.key("automation"),
        CustomItemStack(
            SlimefunUtils.getCustomHead(Heads.PC),
            "&fAutomation"
        )
    )

    val COMPONENTS = SubItemGroup(
        pluginInstance.key("automation_components"),
        MAIN,
        CustomItemStack(
            Material.REDSTONE,
            "&bAutomation Components"
        )
    )
}