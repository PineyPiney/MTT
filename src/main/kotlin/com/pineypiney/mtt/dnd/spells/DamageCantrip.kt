package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.component.DamageRolls
import com.pineypiney.mtt.dnd.DamageType
import com.pineypiney.mtt.dnd.characters.Character

open class DamageCantrip(id: String, val damageType: DamageType, val dice: Int, val baseRolls: Int, settings: Settings) :
	DamageSpell(id, 0, settings) {

	override fun getDamage(caster: Character, level: Int): DamageRolls {
		val bonus = (caster.getLevel() + 1) / 6
		return DamageRolls(damageType, baseRolls + bonus, dice, 0)
	}
}