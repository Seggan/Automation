package io.github.seggan.automation.software

import io.github.seggan.metis.runtime.State
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition

data class CpuJob(val block: BlockPosition, val state: State, var lastTicked: Long, val interval: Long)
