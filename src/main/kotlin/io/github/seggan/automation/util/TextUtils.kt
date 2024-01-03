package io.github.seggan.automation.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

val Component.text: String
    get() = PlainTextComponentSerializer.plainText().serialize(this)