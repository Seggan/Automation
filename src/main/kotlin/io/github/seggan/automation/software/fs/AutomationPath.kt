package io.github.seggan.automation.software.fs

import java.net.URI
import java.nio.file.*
import kotlin.io.path.absolute

class AutomationPath(
    private val path: List<String>,
    private val absolute: Boolean,
    private val fs: AutomationFS
) : Path {

    override fun compareTo(other: Path): Int {
        if (other !is AutomationPath) return -1
        for (i in path.indices) {
            if (i >= other.path.size) return 1
            val a = path[i]
            val b = other.path[i]
            val cmp = a.compareTo(b)
            if (cmp != 0) return cmp
        }
        return path.size.compareTo(other.path.size)
    }

    override fun register(
        watcher: WatchService,
        events: Array<out WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier?
    ): WatchKey = throw UnsupportedOperationException()

    override fun getFileSystem(): FileSystem = fs

    override fun isAbsolute(): Boolean = absolute

    override fun getRoot(): Path? {
        if (!absolute) return null
        return AutomationPath(listOf(path.first()), true, fs)
    }

    override fun getFileName(): Path? {
        if (path.isEmpty()) return null
        return AutomationPath(listOf(path.last()), false, fs)
    }

    override fun getParent(): Path? {
        if (path.size == 1) return null
        return AutomationPath(path.dropLast(1), absolute, fs)
    }

    override fun getNameCount(): Int = path.size

    override fun getName(index: Int): Path = AutomationPath(
        listOf(path[index]),
        absolute && index == 0,
        fs
    )

    override fun subpath(beginIndex: Int, endIndex: Int): Path = AutomationPath(
        path.subList(beginIndex, endIndex),
        absolute && beginIndex == 0,
        fs
    )

    override fun startsWith(other: Path): Boolean {
        if (other !is AutomationPath) return false
        if (other.path.size > path.size) return false
        for (i in other.path.indices) {
            if (path[i] != other.path[i]) return false
        }
        return true
    }

    override fun endsWith(other: Path): Boolean {
        if (other !is AutomationPath) return false
        if (other.path.size > path.size) return false
        var last = path.lastIndex
        for (i in other.path.lastIndex downTo 0) {
            if (path[last] != other.path[i]) return false
            last--
        }
        return true
    }

    override fun normalize(): Path {
        val newPath = mutableListOf<String>()
        for (part in path) {
            when (part) {
                "." -> {
                    // Do nothing
                }
                ".." -> {
                    if (newPath.isEmpty()) {
                        throw NoSuchFileException(path.joinToString("/"))
                    }
                    newPath.removeLast()
                }
                else -> {
                    newPath.add(part)
                }
            }
        }
        return AutomationPath(newPath, absolute, fs)
    }

    override fun resolve(other: Path): Path {
        if (other !is AutomationPath) return other
        if (other.isAbsolute) return other
        return AutomationPath(path + other.path, absolute, fs)
    }

    override fun relativize(other: Path): Path {
        check(other is AutomationPath)
        check(!(absolute && other.absolute)) { "Cannot relativize two absolute paths" }
        val result = ArrayDeque(other.path)
        for (name in path) {
            if (result.isEmpty()) break
            if (result.first() == name) {
                result.removeFirst()
            } else {
                break
            }
        }
        return AutomationPath(result.toList(), false, fs)
    }

    override fun toUri(): URI = URI(fs.provider().scheme,"/${path.joinToString("/")}", null)

    override fun toAbsolutePath(): Path = if (absolute) this else AutomationPath(path, true, fs)

    override fun toRealPath(vararg options: LinkOption?): Path {
        val normalized = normalize().absolute()
        fs.provider().checkAccess(normalized)
        return normalized
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AutomationPath) return false
        return path == other.path && absolute == other.absolute
    }

    override fun hashCode(): Int = path.hashCode() * 31 + absolute.hashCode()

    override fun toString(): String = buildString {
        if (absolute) append("/")
        append(baseToString())
    }

    internal fun baseToString(): String = path.joinToString("/")

    companion object {
        fun convert(path: Path, fs: AutomationFS): AutomationPath {
            if (path is AutomationPath) return path
            return fs.getPath(path.toString())
        }
    }
}