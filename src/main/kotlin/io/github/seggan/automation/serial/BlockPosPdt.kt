package io.github.seggan.automation.serial

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import org.bukkit.Bukkit
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

object BlockPosPdt : PersistentDataType<LongArray, BlockPosition> {

    override fun getPrimitiveType(): Class<LongArray> = LongArray::class.java

    override fun getComplexType(): Class<BlockPosition> = BlockPosition::class.java

    override fun toPrimitive(complex: BlockPosition, context: PersistentDataAdapterContext): LongArray {
        val id = complex.world.uid
        return longArrayOf(id.mostSignificantBits, id.leastSignificantBits, complex.position)
    }

    override fun fromPrimitive(primitive: LongArray, context: PersistentDataAdapterContext): BlockPosition {
        val id = UUID(primitive[0], primitive[1])
        return BlockPosition(Bukkit.getWorld(id)!!, primitive[2])
    }
}