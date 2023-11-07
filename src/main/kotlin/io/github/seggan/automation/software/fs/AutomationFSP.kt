package io.github.seggan.automation.software.fs

import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileAttributeView
import java.nio.file.spi.FileSystemProvider
import kotlin.io.path.*

@OptIn(ExperimentalPathApi::class)
class AutomationFSP(private val limit: Long) : FileSystemProvider() {

    private lateinit var root: Path
    private lateinit var fs: AutomationFS

    private val pathMap = mutableMapOf<Path, Path>()

    override fun getScheme(): String = "automation"

    override fun newFileSystem(uri: URI, env: MutableMap<String, *>): AutomationFS =
        newFileSystem(Paths.get(uri), env)

    override fun newFileSystem(path: Path, env: MutableMap<String, *>?): AutomationFS {
        path.createDirectories()
        root = path
        fs = AutomationFS(this)
        reindex()
        return fs
    }

    fun reindex() {
        pathMap.clear()
        pathMap[fs.root] = root
        root.walk(PathWalkOption.INCLUDE_DIRECTORIES, PathWalkOption.FOLLOW_LINKS).forEach { p ->
            if (p == root) return@forEach
            val relative = root.relativize(p)
            val automationPath = AutomationPath.convert(relative, fs)
            pathMap[automationPath.normalize().absolute()] = relative
        }
    }

    override fun getFileSystem(uri: URI): AutomationFS {
        if (!this::fs.isInitialized) {
            return newFileSystem(uri, mutableMapOf("create" to "true"))
        }
        return fs
    }

    override fun getPath(uri: URI): Path {
        check(uri.scheme == scheme)
        return fs.getPath(uri.path)
    }

    override fun newByteChannel(
        path: Path,
        options: MutableSet<out OpenOption>,
        vararg attrs: FileAttribute<*>
    ): SeekableByteChannel {
        val realPath = path.getRealOrNull()
        if (StandardOpenOption.CREATE in options) {
            if (StandardOpenOption.CREATE_NEW in options && realPath != null)
                throw FileAlreadyExistsException(path.toString())
            if (realPath == null) {
                var s = path.toString()
                if (path.isAbsolute) {
                    s = s.substring(1)
                }
                pathMap[path.normalize().absolute()] = Path(s)
            }
        }
        return LimitingChannel(
            Files.newByteChannel(path.getRealPath(), options, *attrs),
            limit - root.walk().filter(Path::isRegularFile).sumOf(Path::fileSize)
        )
    }

    override fun newDirectoryStream(dir: Path, filter: DirectoryStream.Filter<in Path>): DirectoryStream<Path> =
        Files.newDirectoryStream(dir.getRealPath(), filter)

    override fun createDirectory(dir: Path, vararg attrs: FileAttribute<*>) {
        if (dir.getRealOrNull() != null) throw FileAlreadyExistsException(dir.toString())
        var s = dir.toString()
        if (dir.isAbsolute) {
            s = s.substring(1)
        }
        val realRealPath = root.resolve(s)
        realRealPath.createFile()
        pathMap[dir.normalize().absolute()] = root.relativize(realRealPath)
    }

    override fun delete(path: Path) {
        val realPath = path.getRealPath()
        realPath.deleteExisting()
        pathMap.remove(path.normalize().absolute())
    }

    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        val realSource = source.getRealPath()
        val realTarget = target.getRealPath()
        if (!realSource.exists()) throw NoSuchFileException(source.toString())
        if (realTarget.exists()) throw FileAlreadyExistsException(target.toString())
        realSource.copyTo(realTarget)
        pathMap[target.normalize().absolute()] = realTarget
    }

    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        val realSource = source.getRealPath()
        val realTarget = target.getRealPath()
        if (!realSource.exists()) throw NoSuchFileException(source.toString())
        if (realTarget.exists()) throw FileAlreadyExistsException(target.toString())
        realSource.moveTo(realTarget)
        pathMap[target.normalize().absolute()] = realTarget
        pathMap.remove(source.normalize().absolute())
    }

    override fun isSameFile(path: Path, path2: Path): Boolean = path.getRealPath() == path2.getRealPath()

    override fun isHidden(path: Path): Boolean = path.getRealPath().isHidden()

    override fun getFileStore(path: Path): FileStore = path.getRealPath().fileSystem.fileStores.first()

    override fun checkAccess(path: Path, vararg modes: AccessMode) =
        FileSystems.getDefault().provider().checkAccess(path.getRealPath(), *modes)

    override fun <V : FileAttributeView> getFileAttributeView(
        path: Path,
        type: Class<V>,
        vararg options: LinkOption
    ): V = FileSystems.getDefault().provider().getFileAttributeView(path.getRealPath(), type, *options)

    override fun <A : BasicFileAttributes> readAttributes(
        path: Path,
        type: Class<A>,
        vararg options: LinkOption
    ): A = FileSystems.getDefault().provider().readAttributes(path.getRealPath(), type, *options)

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): MutableMap<String, Any> =
        FileSystems.getDefault().provider().readAttributes(path.getRealPath(), attributes, *options)

    override fun setAttribute(path: Path, attribute: String, value: Any, vararg options: LinkOption) =
        FileSystems.getDefault().provider().setAttribute(path.getRealPath(), attribute, value, *options)

    private fun Path.getRealPath(): Path = getRealOrNull() ?: throw NoSuchFileException(this.toString())

    private fun Path.getRealOrNull(): Path? {
        if (this !is AutomationPath) return this
        return pathMap[this.normalize().absolute()]?.let { this@AutomationFSP.root.resolve(it) }
    }
}