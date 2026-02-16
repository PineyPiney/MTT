package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.component.DamageRolls
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.combat.CombatManager
import net.minecraft.util.math.Vec3d

abstract class DamageSpell(id: String, level: Int, settings: Settings) : Spell(id, level, settings) {

	abstract fun getDamage(caster: Character, level: Int): DamageRolls

	override fun cast(caster: Character, location: Vec3d, level: Int, combat: CombatManager?) {
		val characters = combat?.combatants ?: caster.engine.getAllCharacters()
		for (character in characters) {
			val direction = (location.subtract(caster.pos)).yawAndPitch.x
			if (settings.shape.isIn(location, direction, character.pos)) {
				character.damage(getDamage(caster, level), false, caster)
			}
		}
	}
}