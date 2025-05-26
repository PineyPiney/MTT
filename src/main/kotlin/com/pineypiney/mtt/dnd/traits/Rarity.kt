package com.pineypiney.mtt.dnd.traits

import net.minecraft.util.Colors

enum class Rarity(val colour: Int) {
	COMMON(Colors.WHITE),
	UNCOMMON(Colors.GREEN),
	RARE(Colors.BLUE),
	VERY_RARE(0xB000E0),
	LEGENDARY(0xFFFF8800u.toInt()),
	ARTIFACT(0xA06B45);

	fun lowercase() = name.lowercase()

	companion object {
		fun getLevel(level: Int) = entries.getOrNull(level) ?: COMMON
	}
}