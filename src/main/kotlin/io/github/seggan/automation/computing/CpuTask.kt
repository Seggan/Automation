package io.github.seggan.automation.computing

import io.github.seggan.automation.computing.peripherals.Peripherals
import io.github.seggan.automation.items.PeripheralUpgrade
import io.github.seggan.automation.pluginInstance
import io.github.seggan.metis.runtime.MetisRuntimeException
import io.github.seggan.metis.runtime.State
import io.github.seggan.metis.runtime.chunk.StepResult
import io.github.seggan.metis.util.MetisException
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import java.util.concurrent.CopyOnWriteArrayList

object CpuTask : Runnable {

    @Volatile
    var shutDown = false

    val jobs: MutableList<CpuJob> = CopyOnWriteArrayList()

    override fun run() {
        while (!shutDown) {
            for (job in jobs) {
                if (shutDown) break
                val now = System.nanoTime()
                if (now - job.lastTicked >= job.interval) {
                    job.lastTicked = now
                    try {
                        if (job.state.step() == StepResult.FINISHED) {
                            stopJob(job)
                        }
                    } catch (e: MetisException) {
                        val stderr = job.state.stderr
                        stderr.write(e.report("<N/A>").toByteArray())
                        stderr.flush()
                        stopJob(job)
                        if (e is MetisRuntimeException && e.type == "InternalError") {
                            pluginInstance.runOnNextTick {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        stopJob(job)
                        pluginInstance.runOnNextTick {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    fun stopJob(job: CpuJob) {
        jobs.remove(job)

        val state = job.state
        state.stdout.close()
        state.stderr.close()
        state.stdin.close()

        val block = job.block
        Peripherals.removePeripherals(block)
    }

    fun stopJob(block: BlockPosition) {
        for (job in jobs) {
            if (job.block == block) {
                stopJob(job)
                return
            }
        }
    }

    fun getPositionOfState(state: State): BlockPosition {
        for (job in jobs) {
            if (job.state == state) {
                return job.block
            }
        }
        state.parentState?.let { return getPositionOfState(it) }
        throw IllegalArgumentException("The given state is not associated with a block")
    }

    fun getUpgrades(block: BlockPosition): Set<PeripheralUpgrade> {
        for (job in jobs) {
            if (job.block == block) {
                return job.upgrades
            }
        }
        throw IllegalArgumentException("The given block is not associated with a state")
    }
}