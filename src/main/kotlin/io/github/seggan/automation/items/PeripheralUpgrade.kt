package io.github.seggan.automation.items

enum class PeripheralUpgrade(val id: String) {
    INVENTORY_SCANNER("INVENTORY_SCANNER"),
    ;
    companion object {
        fun fromId(id: String): PeripheralUpgrade? {
            return entries.find { it.id == id }
        }
    }
}