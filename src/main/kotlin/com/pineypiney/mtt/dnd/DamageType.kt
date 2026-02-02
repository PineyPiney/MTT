package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.util.Icon
import com.pineypiney.mtt.util.Icons
import net.minecraft.text.Text
import net.minecraft.util.Colors

class DamageType(val id: String, val colour: Int, val icon: Icon) {

	fun getText() = Text.translatable("mtt.damage_type.$id").withColor(colour).append(icon.toText())

	companion object {

		val list = mutableListOf<DamageType>()
		fun register(name: String, colour: Int, icon: Icon) = DamageType(name, colour, icon).also { list.add(it) }

		val SLASHING = register("slashing", Colors.LIGHT_GRAY, Icons.SLASHING)
		val BLUDGEONING = register("bludgeoning", Colors.LIGHT_GRAY, Icons.BLUDGEONING)
		val PIERCING = register("piercing", Colors.LIGHT_GRAY, Icons.PIERCING)
		val FIRE = register("fire", 0xFFFF8800u.toInt(), Icons.SLASHING)

		fun find(name: String) = list.firstOrNull { it.id == name } ?: SLASHING
	}
}