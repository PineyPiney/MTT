package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.component.DamageRolls
import com.pineypiney.mtt.dnd.DamageType
import com.pineypiney.mtt.dnd.characters.Character

class BasicDamageSpell(
	id: String,
	level: Int,
	val type: DamageType,
	val dice: Int,
	val baseRolls: Int,
	settings: Settings
) : DamageSpell(id, level, settings) {

	override fun getDamage(caster: Character, level: Int): DamageRolls {
		val bonus = (level + 1) / 6
		return DamageRolls(type, baseRolls + bonus, dice, 0)
	}
}