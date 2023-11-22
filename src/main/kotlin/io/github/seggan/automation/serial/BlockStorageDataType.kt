package io.github.seggan.automation.serial

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

interface BlockStorageDataType<T> {

    fun serialize(value: T): String

    fun deserialize(value: String): T?

    companion object {
        val STRING: BlockStorageDataType<String> = BasicBlockStorageDataType(String::toString, String::toString)

        val BOOLEAN: BlockStorageDataType<Boolean> = BasicBlockStorageDataType(
            Boolean::toString,
            String::toBooleanStrictOrNull
        )

        val INT: BlockStorageDataType<Int> = BasicBlockStorageDataType(
            Int::toString,
            String::toIntOrNull
        )

        val DOUBLE: BlockStorageDataType<Double> = BasicBlockStorageDataType(
            Double::toString,
            String::toDoubleOrNull
        )

        val UUID: BlockStorageDataType<UUID> = UuidDataType

        val PLAYER: BlockStorageDataType<OfflinePlayer> = BasicCompositeBlockStorageDataType(
            UUID,
            OfflinePlayer::getUniqueId,
            Bukkit::getOfflinePlayer
        )
    }
}

class BasicBlockStorageDataType<T>(
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T?
) : BlockStorageDataType<T> {
    override fun serialize(value: T): String = serializer(value)

    override fun deserialize(value: String): T? = deserializer(value)
}

abstract class CompositeBlockStorageDataType<S, T>(
    private val intermediate: BlockStorageDataType<T>
) : BlockStorageDataType<S> {

    final override fun serialize(value: S): String = intermediate.serialize(partSerialize(value))
    final override fun deserialize(value: String): S? {
        return partDeserialize(intermediate.deserialize(value) ?: return null)
    }

    abstract fun partSerialize(value: S): T
    abstract fun partDeserialize(value: T): S?
}

class BasicCompositeBlockStorageDataType<S, T>(
    intermediate: BlockStorageDataType<T>,
    private val serializer: (S) -> T,
    private val deserializer: (T) -> S?
) : CompositeBlockStorageDataType<S, T>(intermediate) {

    override fun partSerialize(value: S): T = serializer(value)

    override fun partDeserialize(value: T): S? = deserializer(value)
}

private object UuidDataType : BlockStorageDataType<UUID> {

    private val uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex()

    override fun serialize(value: UUID): String = value.toString()

    override fun deserialize(value: String): UUID? {
        if (!uuidRegex.matches(value)) return null
        return UUID.fromString(value)
    }
}