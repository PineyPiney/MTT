package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.traits.ProficiencyTrait
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency

object Wizard : DNDClass("wizard", 6) {
	override val coreTraits: List<Trait<*>> = listOf(
		ProficiencyTrait("ability", Proficiency.INTELLIGENCE, Proficiency.WISDOM),
		ProficiencyTrait("skill", 2, Proficiency.ARCANA, Proficiency.HISTORY, Proficiency.INSIGHT, Proficiency.INVESTIGATION, Proficiency.MEDICINE, Proficiency.NATURE, Proficiency.RELIGION),
		ProficiencyTrait("weapon", Proficiency.SIMPLE),
	)
	override val multiclassTraits: List<Trait<*>> = listOf()
	override val features: List<List<Feature>> = listOf(

	)
}