package io.github.seggan.automation.computing

import io.github.seggan.metis.runtime.chunk.StepResult
import io.github.seggan.metis.util.MetisException
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
                        e.printStackTrace(PrintWriter(job.state.stderr))
                        jobs.remove(job)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        jobs.remove(job)
                    }
                }
            }
        }
    }

    fun stopJob(job: CpuJob) {
        jobs.remove(job)
        job.state.stdin.close()
        job.state.stdout.close()
        job.state.stderr.close()
    }
}