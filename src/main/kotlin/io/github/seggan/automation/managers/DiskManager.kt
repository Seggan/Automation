package io.github.seggan.automation.managers

import io.github.seggan.automation.items.Disk
import io.github.seggan.automation.pluginInstance
import io.github.seggan.automation.software.fs.AutomationFS
import io.github.seggan.automation.software.fs.AutomationFSP
import io.github.seggan.automation.serial.UuidPdt
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.nio.file.FileSystem
import java.util.UUID
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

object DiskManager {

    private val disks = mutableMapOf<UUID, AutomationFS>()

    private val diskKey = pluginInstance.key("disk-id")
    private val sizeKey = pluginInstance.key("disk-size")

    fun getDisk(uuid: UUID, limit: Long): FileSystem = disks.getOrPut(uuid) {
        AutomationFSP(limit).newFileSystem(
            pluginInstance.diskDir.resolve(uuid.toString()),
            mutableMapOf("create" to "true")
        )
    }

    fun getDisk(item: ItemStack): FileSystem? {
        val pdc = item.itemMeta.persistentDataContainer
        val uuid = pdc.get(diskKey, UuidPdt) ?: return null
        val limit = pdc.get(sizeKey, PersistentDataType.LONG) ?: return null
        return getDisk(uuid, limit)
    }

    @OptIn(ExperimentalPathApi::class)
    fun delete(uuid: UUID) {
        val fs = disks.remove(uuid) ?: return
        fs.provider().root.deleteRecursively()
        fs.close()
    }

    fun createDisk(size: Long): ItemStack {
        val uuid = UUID.randomUUID()
        getDisk(uuid, size)
        val item = Disk(size, uuid)
        val meta = item.itemMeta
        val pdc = meta.persistentDataContainer
        pdc.set(diskKey, UuidPdt, uuid)
        pdc.set(sizeKey, PersistentDataType.LONG, size)
        item.itemMeta = meta
        return item
    }
}