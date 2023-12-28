package io.github.seggan.automation.computing.fs

import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TestFS {

    private val fs = AutomationFSP(Long.MAX_VALUE).newFileSystem(Path(""), mutableMapOf("create" to "true"))

    @Test
    fun testPathParsing() {
        val path = fs.getPath("/home/user")
        assertTrue(path.isAbsolute)
        assertEquals(listOf("home", "user"), path.path)
        assertEquals(fs.getPath("/home"), path.root)
        assertEquals(fs.getPath("user"), path.fileName)
        assertEquals(fs.getPath("/home"), path.parent)
        assertEquals(2, path.nameCount)
        assertEquals(fs.getPath("home"), path.getName(0))
        assertEquals(fs.getPath("user"), path.getName(1))
        assertEquals(fs.getPath("home"), path.subpath(0, 1))
        assertEquals(fs.getPath("home/user"), path.subpath(0, 2))
        assertEquals(fs.getPath("user"), path.subpath(1, 2))
    }
}