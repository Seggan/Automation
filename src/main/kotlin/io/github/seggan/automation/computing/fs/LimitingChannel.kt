package io.github.seggan.automation.computing.fs

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

class LimitingChannel(private val channel: SeekableByteChannel, private val limit: Long) : SeekableByteChannel by channel {

    override fun write(src: ByteBuffer): Int {
        val remaining = limit - channel.position()
        if (remaining <= 0) return 0
        val slice = src.slice()
        return channel.write(slice.limit(remaining.toInt().coerceAtMost(slice.capacity()))).also {
            src.position(src.position() + it)
        }
    }
}