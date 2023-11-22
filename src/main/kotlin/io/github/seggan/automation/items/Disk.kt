package io.github.seggan.automation.items

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import org.bukkit.Material
import java.util.*

class Disk(
    size: Long,
    uuid: UUID
) : SlimefunItemStack(
    "AUTOMATION_FLOPPY_DISK",
    Material.MUSIC_DISC_5,
    "&fFloppy Disk",
    "",
    "&7A floppy disk that can store data.",
    "",
    "&eSize: $size bytes",
    "&eID: $uuid",
)