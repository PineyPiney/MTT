package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.component.DamageRolls
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.combat.CombatManager
import com.pineypiney.mtt.dnd.traits.Ability
import net.minecraft.util.math.Vec3d

abstract class DamageSpell(id: String, level: Int, settings: Settings) : Spell(id, level, settings) {

	abstract fun getDamage(caster: Character, level: Int): DamageRolls

	override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {
		val characters = combat?.combatants ?: caster.engine.getAllCharacters()
		for (character in characters) {
			if (settings.shape.isIn(location, direction, character.pos)) {
				apply(caster, character, level, spellCastingAbility)
			}
		}
	}

	open fun apply(caster: Character, target: Character, level: Int, spellCastingAbility: Ability) {
		target.damage(getDamage(caster, level), false, caster, getSavingThrow(caster, spellCastingAbility))
	}
}