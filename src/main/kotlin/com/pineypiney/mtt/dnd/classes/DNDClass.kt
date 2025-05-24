package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.CharacterSheet
import com.pineypiney.mtt.dnd.traits.features.Feature

abstract class DNDClass(val id: String, val healthDie: Int) {

	abstract val features: List<List<Feature>>
	abstract val proficiencies: List<String>

	abstract fun onMultiClass(sheet: CharacterSheet)

	companion object {
		val classes = listOf(Barbarian, Fighter, Ranger, Wizard)
	}
}