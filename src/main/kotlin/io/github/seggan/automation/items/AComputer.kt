package io.github.seggan.automation.items

import io.github.seggan.automation.util.storeBoolean
import io.github.seggan.automation.util.storePlayer
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class AComputer(
    group: ItemGroup,
    item: SlimefunItemStack,
    type: RecipeType,
    recipe: Array<ItemStack?>
) : SlimefunItem(group, item, type, recipe) {

    abstract val title: String

    override fun preRegister() {
        object : BlockMenuPreset(id, title) {
            override fun init() {
                drawBackground(BG)
                for (item in INV_BORDER) {
                    addItem(item, INV_ITEM, ChestMenuUtils.getEmptyClickHandler())
                }
                for (item in COMPONENT_BORDER) {
                    addItem(item, COMPONENT_ITEM, ChestMenuUtils.getEmptyClickHandler())
                }
                addItem(SWITCH, OFF)
            }

            override fun newInstance(menu: BlockMenu, b: Block) {
                menu.addMenuClickHandler(SWITCH) { p, s, item, _ ->
                    when (item) {
                        OFF -> {
                            menu.replaceExistingItem(s, ON)
                            b.storeBoolean("on", true)
                        }
                        ON -> {
                            menu.replaceExistingItem(s, OFF)
                            b.storeBoolean("on", false)
                        }
                        else -> throw AssertionError("Unreachable code")
                    }
                    b.storePlayer("owner", p)
                    false
                }
            }

            override fun canOpen(block: Block, player: Player): Boolean {
                return Slimefun.getProtectionManager().hasPermission(player, block, Interaction.INTERACT_BLOCK)
            }

            override fun getSlotsAccessedByItemTransport(flow: ItemTransportFlow) = intArrayOf()
        }
    }

    abstract fun onPowerOnOff(value: Boolean)

    companion object {
        private val INV_BORDER = intArrayOf(5, 14, 23, 32, 41, 50)
        private val INV_ITEM = CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&fInventory")
        private val COMPONENT_BORDER = intArrayOf(18, 19, 20, 21, 22)
        private val COMPONENT_ITEM = CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, "&fComponents")
        private val BG = intArrayOf(0, 2, 4, 9, 10, 11, 12, 13)
        private const val STATUS = 1
        private const val SWITCH = 3
        private val ON = CustomItemStack(Material.STRUCTURE_VOID, "&aClick to turn off")
        private val OFF = CustomItemStack(Material.BARRIER, "&cClick to turn on")
    }
}