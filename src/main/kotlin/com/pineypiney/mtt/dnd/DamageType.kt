package com.pineypiney.mtt.dnd

import net.minecraft.util.Colors

class DamageType(val id: String, val colour: Int) {

	companion object {

		val list = mutableListOf<DamageType>()
		fun register(name: String, colour: Int) = DamageType(name, colour).also { list.add(it) }

		val SLASHING = register("slashing", Colors.LIGHT_GRAY)
		val BLUDGEONING = register("bludgeoning", Colors.LIGHT_GRAY)
		val PIERCING = register("piercing", Colors.LIGHT_GRAY)
		val FIRE = register("fire", 0xFFFF8800u.toInt())

		fun find(name: String) = list.firstOrNull() { it.id == name } ?: SLASHING
	}
}