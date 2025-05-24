package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.CharacterSheet
import com.pineypiney.mtt.dnd.traits.features.Feature

object Wizard : DNDClass("wizard", 6) {
	override val proficiencies: List<String> = listOf("dagger", "dart", "sling", "quarter_staff", "light_crossbow")
	override val features: List<List<Feature>> = listOf(

	)

	override fun onMultiClass(sheet: CharacterSheet) {

	}
}