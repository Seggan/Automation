package io.github.seggan.automation.util

import net.kyori.adventure.text.Component
import org.bukkit.map.MinecraftFont

private val lineWidth = MinecraftFont.Font.getWidth("LLLLLLLLLLLLLLLLLLL")

fun String.paginate(): List<Component>? {
    val lines = mutableListOf<Component>()
    for (section in lines()) {
        var line = section
        while (line.isNotEmpty()) {
            var width = 0
            var index = 0
            do {
                width += MinecraftFont.Font.getChar(section[index++])?.width ?: return null
            } while (width < lineWidth && index < section.length)
            lines.add(Component.text(line.substring(0, index)))
            line = line.substring(index)
        }
        lines.add(Component.empty())
    }
    return lines
}