package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.CharacterSheet
import com.pineypiney.mtt.dnd.traits.features.Feature

object Ranger : DNDClass("ranger", 10) {
	override val proficiencies: List<String> = listOf("light_armour", "medium_armour", "shields", "simple_weapons", "martial_weapons")
	override val features: List<List<Feature>> = listOf(

	)

	override fun onMultiClass(sheet: CharacterSheet) {
		sheet.proficiencies.addAll(proficiencies)
		// Also add a skill from ranger skill list
	}
}