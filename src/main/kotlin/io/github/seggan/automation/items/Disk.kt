package io.github.seggan.automation.items

import io.github.seggan.automation.pluginInstance
import io.github.seggan.automation.software.fs.AutomationFS
import io.github.seggan.automation.software.fs.AutomationFSP
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists

class Disk(delegate: ItemStack) {

    val id: UUID
    val underlyingFile: Path
    val fs: AutomationFS

    val size: Long

    init {
        val pdc = delegate.itemMeta.persistentDataContainer
        id = UUID.fromString(pdc.get(UUID_KEY, PersistentDataType.STRING))
        underlyingFile = pluginInstance.diskDir.resolve("$id.zip")
        size = pdc.get(SIZE_KEY, PersistentDataType.LONG)!!
        fs = AutomationFSP(size).newFileSystem(underlyingFile, mutableMapOf("create" to "true")) as AutomationFS
    }

    fun destroy() {
        fs.close()
        underlyingFile.deleteIfExists()
    }

    companion object {
        private val UUID_KEY = pluginInstance.key("disk-id")
        private val SIZE_KEY = pluginInstance.key("disk-size")
    }
}
