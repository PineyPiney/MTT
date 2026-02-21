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
	override val features: List<List<Feature>> = listOf()

	fun getNumCantrips(level: Int) = when {
		level < 4 -> 3
		level < 10 -> 4
		else -> 5
	}

	fun getNumPreparedSpells(level: Int) = when {
		level < 5 -> level + 3
		level < 9 -> level + 4
		level < 12 -> level + 5
		level < 16 -> level + 4
		else -> level + 5
	}
}