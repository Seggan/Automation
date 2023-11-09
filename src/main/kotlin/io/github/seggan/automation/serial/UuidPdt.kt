package io.github.seggan.automation.serial

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

object UuidPdt : PersistentDataType<LongArray, UUID> {

    override fun getPrimitiveType(): Class<LongArray> = LongArray::class.java

    override fun getComplexType(): Class<UUID> = UUID::class.java

    override fun fromPrimitive(primitive: LongArray, context: PersistentDataAdapterContext): UUID {
        return UUID(primitive[0], primitive[1])
    }

    override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): LongArray {
        return longArrayOf(complex.mostSignificantBits, complex.leastSignificantBits)
    }
}