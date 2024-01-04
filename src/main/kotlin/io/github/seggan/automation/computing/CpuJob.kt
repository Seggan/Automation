package io.github.seggan.automation.computing

import io.github.seggan.automation.items.PeripheralUpgrade
import io.github.seggan.metis.runtime.State
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition

data class CpuJob(
    val block: BlockPosition,
    val state: State,
    val upgrades: Set<PeripheralUpgrade>,
    var lastTicked: Long,
    val interval: Long
)
