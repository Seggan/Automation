package io.github.seggan.automation.util

import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import java.util.*

fun Location.storeString(key: String, value: String) = BlockStorage.addBlockInfo(this, key, value)
fun Block.storeString(key: String, value: String) = location.storeString(key, value)
fun Location.loadString(key: String): String = BlockStorage.getLocationInfo(this, key)
fun Block.loadString(key: String): String = location.loadString(key)

fun Location.storeBoolean(key: String, value: Boolean) = storeString(key, value.toString())
fun Block.storeBoolean(key: String, value: Boolean) = location.storeBoolean(key, value)
fun Location.loadBoolean(key: String): Boolean = loadString(key).toBoolean()
fun Block.loadBoolean(key: String): Boolean = location.loadBoolean(key)

fun Location.storeInt(key: String, value: Int) = storeString(key, value.toString())
fun Block.storeInt(key: String, value: Int) = location.storeInt(key, value)
fun Location.loadInt(key: String): Int = loadString(key).toInt()
fun Block.loadInt(key: String): Int = location.loadInt(key)

fun Location.storeDouble(key: String, value: Double) = storeString(key, value.toString())
fun Block.storeDouble(key: String, value: Double) = location.storeDouble(key, value)
fun Location.loadDouble(key: String): Double = loadString(key).toDouble()
fun Block.loadDouble(key: String): Double = location.loadDouble(key)

fun Location.storePlayer(key: String, value: OfflinePlayer) = storeString(key, value.uniqueId.toString())
fun Block.storePlayer(key: String, value: OfflinePlayer) = location.storePlayer(key, value)
fun Location.loadPlayer(key: String): OfflinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(loadString(key)))
fun Block.loadPlayer(key: String): OfflinePlayer = location.loadPlayer(key)