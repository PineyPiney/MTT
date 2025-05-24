package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.CharacterSheet
import com.pineypiney.mtt.dnd.stats.Ability
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.features.Features

object Barbarian : DNDClass("barbarian", 12) {

	override val proficiencies: List<String> = listOf("light_armour", "medium_armour", "shields", "simple_weapons", "martial_weapons")
	override val features: List<List<Feature>> = listOf(
		listOf(Features.Rage, Features.UnarmouredDefense(Ability.CONSTITUTION)),
		listOf(Features.RecklessAttack, Features.DangerSense)
	)

	override fun onMultiClass(sheet: CharacterSheet) {
		sheet.proficiencies.addAll(listOf())
	}

}