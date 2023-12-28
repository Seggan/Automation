package io.github.seggan.automation.computing

import io.github.seggan.automation.pluginInstance
import io.github.seggan.metis.runtime.MetisRuntimeException
import io.github.seggan.metis.runtime.State
import io.github.seggan.metis.runtime.chunk.StepResult
import io.github.seggan.metis.util.MetisException
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import java.io.PrintWriter
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
        state.stdout.flush()
        state.stderr.flush()
        state.stdout.close()
        state.stderr.close()
        state.stdin.close()
    }

    fun getLocationOfState(state: State): BlockPosition? {
        state.parentState?.let { return getLocationOfState(it) }
        for (job in jobs) {
            if (job.state == state) {
                return job.block
            }
        }
        return null
    }
}