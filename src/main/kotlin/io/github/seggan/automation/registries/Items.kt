package io.github.seggan.automation.registries

import io.github.seggan.automation.computing.peripherals.Sensor
import io.github.seggan.automation.items.Computer
import io.github.seggan.automation.items.Cpu
import io.github.seggan.automation.items.Disk
import io.github.seggan.automation.items.Peripheral
import io.github.seggan.automation.util.MaterialType
import io.github.seggan.automation.util.buildSlimefunItem
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
object Items {

    //<editor-fold desc="CPUs" defaultstate="collapsed">
    val IRON_CPU = buildSlimefunItem {
        name = "&fIron CPU"
        id = "IRON_CPU"
        material = MaterialType.Head(Heads.IRON_CPU)
        +"&7A CPU made out of iron."
        +"&7It's not very fast, but it's cheap."
        +""
        +"&eClock Speed: 2 Hz"
    }

    val GOLD_CPU = buildSlimefunItem {
        name = "&6Gold CPU"
        id = "GOLD_CPU"
        material = MaterialType.Head(Heads.GOLD_CPU)
        +"&7A CPU made out of gold."
        +"&7It's faster than iron, but still not very fast."
        +""
        +"&eClock Speed: 16 Hz"
    }

    val REDSTONE_CPU = buildSlimefunItem {
        name = "&4Redstone CPU"
        id = "REDSTONE_CPU"
        material = MaterialType.Head(Heads.REDSTONE_CPU)
        +"&7A CPU made out of redstone."
        +"&7A decent CPU for a decent price."
        +""
        +"&eClock Speed: 64 Hz"
    }

    val DIAMOND_CPU = buildSlimefunItem {
        name = "&bDiamond CPU"
        id = "DIAMOND_CPU"
        material = MaterialType.Head(Heads.DIAMOND_CPU)
        +"&7A CPU made out of diamonds."
        +"&7A very fast CPU, but it's expensive."
        +""
        +"&eClock Speed: 256 Hz"
    }

    val REINFORCED_CPU = buildSlimefunItem {
        name = "&7Reinforced CPU"
        id = "REINFORCED_CPU"
        material = MaterialType.Head(Heads.REINFORCED_CPU)
        +"&7A CPU made out of reinforced alloy."
        +"&7Blazing fast, but also very expensive."
        +""
        +"&eClock Speed: 1024 Hz"
    }

    val NPU = buildSlimefunItem {
        name = "&1Nano Processing Unit"
        id = "NPU"
        material = MaterialType.Head(Heads.NPU)
        +"&7A CPU utilizing nanotechnology."
        +"&7Very complex and very expensive. Possibly overkill."
        +""
        +"&eClock Speed: As fast as possible"
    }
    //</editor-fold>

    //<editor-fold desc="Peripherals" defaultstate="collapsed">
    val SENSOR_1 = buildSlimefunItem {
        name = "&fSensor I"
        id = "SENSOR_1"
        material = MaterialType.Material(Material.BEACON)
        +"&7A sensor that can get information about nearby"
        +"&7blocks and entities."
        +""
        +"&eRange: 4 blocks"
    }

    val SENSOR_2 = buildSlimefunItem {
        name = "&fSensor II"
        id = "SENSOR_2"
        material = MaterialType.Material(Material.BEACON)
        +"&7A sensor that can get information about nearby"
        +"&7blocks and entities."
        +""
        +"&eRange: 16 blocks"
    }

    val SENSOR_3 = buildSlimefunItem {
        name = "&fSensor III"
        id = "SENSOR_3"
        material = MaterialType.Material(Material.BEACON)
        +"&7A sensor that can get information about nearby"
        +"&7blocks and entities."
        +""
        +"&eRange: 64 blocks"
    }
    //</editor-fold>

    val DISK: SlimefunItemStack = Disk(512, UUID(0, 0))

    val COMPUTER = buildSlimefunItem {
        name = "&fComputer"
        id = "COMPUTER"
        material = MaterialType.Head(Heads.PC)
        +"&7A computer that can run programs."
    }

    fun register(addon: SlimefunAddon) {
        Cpu(Groups.COMPONENTS, IRON_CPU, 2, RecipeType.NULL, arrayOf()).register(addon)
        Cpu(Groups.COMPONENTS, GOLD_CPU, 16, RecipeType.NULL, arrayOf()).register(addon)
        Cpu(Groups.COMPONENTS, REDSTONE_CPU, 64, RecipeType.NULL, arrayOf()).register(addon)
        Cpu(Groups.COMPONENTS, DIAMOND_CPU, 256, RecipeType.NULL, arrayOf()).register(addon)
        Cpu(Groups.COMPONENTS, REINFORCED_CPU, 1024, RecipeType.NULL, arrayOf()).register(addon)
        Cpu(Groups.COMPONENTS, NPU, Int.MAX_VALUE, RecipeType.NULL, arrayOf()).register(addon)

        Peripheral(Groups.COMPONENTS, SENSOR_1, Sensor(4), RecipeType.NULL, arrayOf()).register(addon)
        Peripheral(Groups.COMPONENTS, SENSOR_2, Sensor(16), RecipeType.NULL, arrayOf()).register(addon)
        Peripheral(Groups.COMPONENTS, SENSOR_3, Sensor(64), RecipeType.NULL, arrayOf()).register(addon)

        SlimefunItem(Groups.COMPONENTS, DISK, RecipeType.NULL, arrayOf()).register(addon)
        Computer(Groups.MACHINES, COMPUTER, RecipeType.NULL, arrayOf()).register(addon)
    }
}