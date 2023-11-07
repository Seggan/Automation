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

    override fun getScheme(): String = "automation"

    override fun newFileSystem(uri: URI, env: MutableMap<String, *>): AutomationFS =
        newFileSystem(Paths.get(uri), env)

    override fun newFileSystem(path: Path, env: MutableMap<String, *>?): AutomationFS {
        path.createDirectories()
        root = path
        fs = AutomationFS(this)
        return fs
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
        val realPath = path.getRealPath()
        if (StandardOpenOption.CREATE_NEW in options && realPath.exists()) {
            throw FileAlreadyExistsException(path.toString())
        }
        return LimitingChannel(
            Files.newByteChannel(realPath, options, *attrs),
            limit - root.walk().filter(Path::isRegularFile).sumOf(Path::fileSize)
        )
    }

    override fun newDirectoryStream(dir: Path, filter: DirectoryStream.Filter<in Path>): DirectoryStream<Path> =
        Files.newDirectoryStream(dir.getRealPath(), filter)

    override fun createDirectory(dir: Path, vararg attrs: FileAttribute<*>) {
        if (dir.exists()) throw FileAlreadyExistsException(dir.toString())
        dir.getRealPath().createDirectory()
    }

    override fun delete(path: Path) {
        if (!path.exists()) throw NoSuchFileException(path.toString())
        path.getRealPath().deleteExisting()
    }

    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        val realSource = source.getRealPath()
        val realTarget = target.getRealPath()
        if (!realSource.exists()) throw NoSuchFileException(source.toString())
        if (realTarget.exists()) throw FileAlreadyExistsException(target.toString())
        realSource.copyTo(realTarget)
    }

    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        val realSource = source.getRealPath()
        val realTarget = target.getRealPath()
        if (!realSource.exists()) throw NoSuchFileException(source.toString())
        if (realTarget.exists()) throw FileAlreadyExistsException(target.toString())
        realSource.moveTo(realTarget)
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

    private fun Path.getRealPath(): Path {
        if (this !is AutomationPath) return this
        return this@AutomationFSP.root.resolve(baseToString())
    }
}