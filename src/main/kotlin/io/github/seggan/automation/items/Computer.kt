package io.github.seggan.automation.items

import io.github.seggan.automation.computing.ChatInputStream
import io.github.seggan.automation.managers.DiskManager
import io.github.seggan.automation.serial.BlockStorageDataType
import io.github.seggan.automation.serial.setBlockStorage
import io.github.seggan.automation.computing.ChatOutputStream
import io.github.seggan.automation.computing.CpuJob
import io.github.seggan.automation.computing.CpuTask
import io.github.seggan.automation.computing.metis.preinit
import io.github.seggan.automation.util.Mutex
import io.github.seggan.automation.util.position
import io.github.seggan.metis.parsing.CodeSource
import io.github.seggan.metis.runtime.State
import io.github.seggan.metis.runtime.chunk.Chunk
import io.github.seggan.metis.util.MetisException
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker
import me.mrCookieSlime.Slimefun.api.BlockStorage
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists

class Computer(
    group: ItemGroup,
    item: SlimefunItemStack,
    type: RecipeType,
    recipe: Array<out ItemStack?>
) : SlimefunItem(group, item, type, recipe), EnergyNetComponent {

    override fun preRegister() {
        object : BlockMenuPreset(id, LegacyComponentSerializer.legacyAmpersand().serialize(item.displayName())) {
            override fun init() {
                drawBackground(BG)
                for (item in INV_BORDER) {
                    addItem(item, INV_ITEM, ChestMenuUtils.getEmptyClickHandler())
                }
                for (item in COMPONENT_BORDER) {
                    addItem(item, COMPONENT_ITEM, ChestMenuUtils.getEmptyClickHandler())
                }
                addItem(SWITCH, OFF)
                addItem(STATUS, STATUS_OFF)
            }

            override fun newInstance(menu: BlockMenu, b: Block) {
                menu.addMenuClickHandler(SWITCH) { p, s, item, _ ->
                    val value = when (item.type) {
                        Material.BARRIER -> {
                            menu.replaceExistingItem(s, ON)
                            true
                        }

                        Material.STRUCTURE_VOID -> {
                            menu.replaceExistingItem(s, OFF)
                            menu.replaceExistingItem(STATUS, STATUS_OFF)
                            false
                        }

                        else -> throw AssertionError()
                    }
                    b.setBlockStorage("on", value, BlockStorageDataType.BOOLEAN)
                    b.setBlockStorage("owner", p, BlockStorageDataType.PLAYER)
                    if (value) {
                        powerOn(b)
                    } else {
                        powerOff(b)
                    }
                    false
                }
            }

            override fun canOpen(block: Block, player: Player): Boolean {
                return Slimefun.getProtectionManager().hasPermission(player, block, Interaction.INTERACT_BLOCK)
            }

            override fun getSlotsAccessedByItemTransport(flow: ItemTransportFlow) = intArrayOf()
        }
        addItemHandler(object : BlockTicker() {

            override fun isSynchronized() = true

            override fun tick(b: Block, item: SlimefunItem, data: Config) {
                if (data.getString("on") == "true") {
                    mainTick(b)
                }
            }
        })
        addItemHandler(object : BlockBreakHandler(false, false) {
            override fun onPlayerBreak(e: BlockBreakEvent, item: ItemStack, drops: MutableList<ItemStack>) =
                onBreak(e.block)
        })
    }

    private fun powerOn(b: Block) {
        val inv = BlockStorage.getInventory(b)
        var cpuSpeed = -1L
        for (component in COMPONENT_SLOTS) {
            val item = inv.getItemInSlot(component)
            val sfItem = getByItem(item)
            if (sfItem is Cpu) {
                cpuSpeed = sfItem.clockInterval
                break
            }
        }
        if (cpuSpeed == -1L) {
            inv.replaceExistingItem(STATUS, STATUS_NO_CPU)
            return
        }
        var initPath: Path? = null
        for (component in COMPONENT_SLOTS) {
            val item = inv.getItemInSlot(component) ?: continue
            val fs = DiskManager.getDisk(item)
            if (fs != null) {
                val path = fs.getPath("init.metis")
                if (path.exists()) {
                    initPath = path
                    break
                }
            }
        }
        if (initPath == null) {
            inv.replaceExistingItem(STATUS, STATUS_NO_DISK)
            return
        }

        val chunk = try {
            Chunk.load(CodeSource.fromPath(initPath))
        } catch (e: MetisException) {
            val error = e.report("init.metis")
                .split("\n")
                .mapTo(mutableListOf()) {
                    Component.text()
                        .color(NamedTextColor.YELLOW)
                        .style(Style.empty())
                        .content(it)
                        .build()
                }
            error.add(0, Component.empty())

            val errorItem = STATUS_ERROR.clone()
            errorItem.lore(error)
            inv.replaceExistingItem(STATUS, errorItem)
            return
        }

        val state = State()
        state.fileSystem = initPath.fileSystem
        state.currentDir = initPath.fileSystem.getPath("/")
        state.stdin = ChatInputStream(b.location)
        state.stdout = ChatOutputStream(b.location, NamedTextColor.WHITE)
        state.stderr = ChatOutputStream(b.location, NamedTextColor.RED)
        state.preinit()
        state.loadChunk(chunk)
        state.call(0)

        val job = CpuJob(b.position, state, 0, cpuSpeed)
        CpuTask.jobs.add(job)
        inv.replaceExistingItem(STATUS, STATUS_OK)
    }

    private fun powerOff(b: Block) {
        val pos = b.position
        CpuTask.jobs.firstOrNull { it.block == pos }?.let(CpuTask::stopJob)
    }

    private fun onBreak(b: Block) {
        val pos = b.position
        CpuTask.jobs.firstOrNull { it.block == pos }?.let(CpuTask::stopJob)
        syncJobs.remove(pos)
    }

    private fun mainTick(b: Block) {
        val loc = b.location
        if (getCharge(loc) < 16) {
            BlockStorage.getInventory(b).replaceExistingItem(STATUS, STATUS_NO_POWER)
            return
        }
        removeCharge(loc, 16)
        val jobs = syncJobs[loc.position] ?: return
        jobs.lock { list ->
            for (job in list) {
                job(b)
            }
            list.clear()
        }
    }

    companion object {
        private val INV_BORDER = intArrayOf(5, 14, 23, 32, 41, 50)
        private val INV_ITEM = CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&fInventory")
        private val COMPONENT_BORDER = intArrayOf(18, 19, 20, 21, 22)
        private val COMPONENT_ITEM = CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, "&fComponents")
        private val COMPONENT_SLOTS = intArrayOf(27, 28, 29, 30, 31, 36, 37, 38, 39, 40, 45, 46, 47, 48, 49)
        private val BG = intArrayOf(0, 2, 4, 9, 10, 11, 12, 13)
        private const val STATUS = 1
        private const val SWITCH = 3
        private val ON = CustomItemStack(Material.STRUCTURE_VOID, "&aClick to turn off")
        private val OFF = CustomItemStack(Material.BARRIER, "&cClick to turn on")

        private val STATUS_NO_CPU = CustomItemStack(Material.RED_STAINED_GLASS, "&cNo CPU")
        private val STATUS_NO_RAM = CustomItemStack(Material.RED_STAINED_GLASS, "&cNo RAM")
        private val STATUS_NO_DISK = CustomItemStack(Material.RED_STAINED_GLASS, "&cNo Disk")
        private val STATUS_NO_POWER = CustomItemStack(Material.RED_STAINED_GLASS, "&cNo Power")
        private val STATUS_ERROR = CustomItemStack(Material.RED_STAINED_GLASS, "&cError")
        private val STATUS_OK = CustomItemStack(Material.GREEN_STAINED_GLASS, "&aOK")
        private val STATUS_OFF = CustomItemStack(Material.GRAY_STAINED_GLASS, "&7Off")

        val syncJobs: MutableMap<BlockPosition, Mutex<MutableList<(Block) -> Unit>>> = ConcurrentHashMap()
    }

    override fun getEnergyComponentType() = EnergyNetComponentType.CONSUMER

    override fun getCapacity() = 32
}