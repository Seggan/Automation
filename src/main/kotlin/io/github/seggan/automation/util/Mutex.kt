package io.github.seggan.automation.util

import java.util.concurrent.locks.ReentrantLock

class Mutex<T>(private val value: T) {

    private val lock = ReentrantLock()

    fun <R> lock(block: (T) -> R): R {
        lock.lock()
        try {
            return block(value)
        } finally {
            lock.unlock()
        }
    }
}