package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.combat.CombatManager
import com.pineypiney.mtt.dnd.network.ServerCharacter
import com.pineypiney.mtt.dnd.rolls.SavingThrow
import com.pineypiney.mtt.dnd.traits.Ability
import net.minecraft.util.math.Vec3d

abstract class Spell(val id: String, val level: Int, val settings: Settings) {

	abstract fun cast(caster: ServerCharacter, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?)

	open fun getSavingAbility(): Ability? = Ability.DEXTERITY

	open fun getSaveThreshold(character: Character, spellCastingAbility: Ability): Int {
		return 8 + character.abilities.getMod(spellCastingAbility) + character.getProficiencyBonus()
	}

	open fun getSavingThrow(character: Character, spellCastingAbility: Ability): SavingThrow? {
		val ability = getSavingAbility() ?: return null
		val target = getSaveThreshold(character, spellCastingAbility)
		return SavingThrow(ability, target)
	}

	open fun getTargetCount(): Int = 1

	fun getCharacter(engine: DNDEngine<*>, location: Vec3d, combat: CombatManager?): Character? {
		return if (combat == null) engine.getAllCharacters().firstOrNull { it.pos == location } ?: return null
		else combat.getCharacterAt(location) ?: return null
	}

	fun forEachCharacter(engine: DNDEngine<*>, combat: CombatManager?, action: (character: Character) -> Unit) {
		if (combat == null) engine.getAllCharacters().forEach(action)
		else combat.forEachCharacter(action)
	}

	fun getTranslationKey() = "mtt.spell.$id"
	fun getDescriptionKey() = "mtt.spell.$id.description"

	override fun toString(): String {
		return "Spell($id)"
	}

	companion object {
		fun findById(id: String): Spell = Spells.map.firstNotNullOfOrNull { it.value.firstOrNull { spell -> spell.id == id } } ?: throw Error("There is no spell with ID $id")
	}

	class Settings {
		var school = School.ABJURATION
		var range = 60
		var shape: SpellShape = SpellShape.Single
		var targetsEntity = false
		var duration = 0
		var concentration = false
		var ritual = false
		val components = Components()

		fun school(school: School) = apply { this.school = school }
		fun range(range: Int) = apply { this.range = range }
		fun shape(shape: SpellShape) = apply { this.shape = shape }
		fun targetsEntity() = apply { targetsEntity = true }
		fun duration(duration: Int) = apply { this.duration = duration }
		fun minutes(minutes: Int) = apply { this.duration = minutes * 60 }
		fun hours(hours: Int) = apply { this.duration = hours * 3600 }
		fun concentration() = apply { this.concentration = true }
		fun ritual() = apply { this.ritual = true }
		fun verbal() = apply { components.verbal = true }
		fun semantic() = apply { components.semantic = true }
		fun material() = apply { components.material = true }
	}

	data class Components(var verbal: Boolean = false, var semantic: Boolean = false, var material: Boolean = false)
}