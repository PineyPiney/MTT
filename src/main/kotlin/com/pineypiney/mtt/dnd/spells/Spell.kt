package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.combat.CombatManager
import net.minecraft.util.math.Vec3d

abstract class Spell(val id: String, val level: Int, val settings: Settings) {

	abstract fun cast(caster: Character, location: Vec3d, level: Int, combat: CombatManager?)

	open fun getTargetCount(): Int = 1

	companion object {
		fun findById(id: String): Spell = Spells.set.first { it.id == id }
	}

	class Settings {
		var school = School.ABJURATION
		var range = 60
		var shape: SpellShape = SpellShape.Single
		var targetsEntity = false
		var concentration = false
		var duration = 0
		val components = Components()

		fun school(school: School) = apply { this.school = school }
		fun range(range: Int) = apply { this.range = range }
		fun shape(shape: SpellShape) = apply { this.shape = shape }
		fun targetsEntity() = apply { targetsEntity = true }
		fun concentration() = apply { this.concentration = true }
		fun duration(duration: Int) = apply { this.duration = duration }
		fun minutes(minutes: Int) = apply { this.duration = minutes * 60 }
		fun hours(hours: Int) = apply { this.duration = hours * 3600 }
		fun verbal() = apply { components.verbal = true }
		fun semantic() = apply { components.semantic = true }
		fun material() = apply { components.material = true }
	}

	data class Components(var verbal: Boolean = false, var semantic: Boolean = false, var material: Boolean = false)
}