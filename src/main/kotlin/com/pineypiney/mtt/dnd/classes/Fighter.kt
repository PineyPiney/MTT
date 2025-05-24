package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.CharacterSheet
import com.pineypiney.mtt.dnd.traits.features.Feature

object Fighter : DNDClass("fighter", 10) {
	override val proficiencies: List<String> = listOf("light_armour", "medium_armour", "heavy_armour", "shields", "simple_weapons", "martial_weapons")
	override val features: List<List<Feature>> = listOf(

	)

	override fun onMultiClass(sheet: CharacterSheet) {
		sheet.proficiencies.addAll(listOf("light_armour", "medium_armour", "shields", "simple_weapons", "martial_weapons"))
	}
}