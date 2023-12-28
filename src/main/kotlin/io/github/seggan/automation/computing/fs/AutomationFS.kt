package io.github.seggan.automation.computing.fs

import java.nio.file.*
import java.nio.file.attribute.UserPrincipalLookupService

class AutomationFS(private val provider: AutomationFSP) : FileSystem() {

    private var closed = false
    val root = AutomationPath(listOf(), true, this)

    override fun close() {
        if (closed) return
        closed = true
    }

    override fun provider(): AutomationFSP = provider
    override fun isOpen(): Boolean = !closed
    override fun isReadOnly(): Boolean = false
    override fun getSeparator(): String = "/"
    override fun getRootDirectories(): MutableIterable<Path> = mutableListOf(root)
    override fun getFileStores(): MutableIterable<FileStore> = FileSystems.getDefault().fileStores
    override fun supportedFileAttributeViews(): MutableSet<String> = mutableSetOf("basic")

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher {
        val split = syntaxAndPattern.split(":")
        var pattern = split.last()
        if (split.first() == "glob") {
            pattern = pattern.replace(".", "\\.")
                .replace("**", ".*")
                .replace("*", "[^/]*")
                .replace("?", ".")
                .replace("[!", "[^")
            pattern = buildString {
                var depth = 0
                for (c in pattern) {
                    if (c == '{') {
                        append("((?:")
                        depth++
                    } else if (c == '}') {
                        append("))")
                        depth--
                    } else if (c == ',' && depth > 0) {
                        append(")|(?:")
                    } else {
                        append(c)
                    }
                }
            }
        }
        if (split.first() != "regex") throw UnsupportedOperationException()
        val regex = pattern.toRegex()
        return PathMatcher { path -> path.toString().matches(regex) }
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService = throw UnsupportedOperationException()
    override fun newWatchService(): WatchService = throw UnsupportedOperationException()

    override fun getPath(first: String, vararg more: String): AutomationPath {
        if (more.isNotEmpty()) return getPath(first + "/" + more.joinToString("/"))
        val input = first.trim()
        if (input == "/") return root
        return AutomationPath(
            pathRegex.findAll(input).map(MatchResult::value).toList(),
            input.startsWith('/'),
            this
        )
    }
}

private val pathRegex = "[^/]+".toRegex()