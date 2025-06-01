package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.traits.ProficiencyTrait
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency

object Ranger : DNDClass("ranger", 10) {
	override val coreTraits: List<Trait<*>> = listOf(
		ProficiencyTrait("ability", Proficiency.STRENGTH, Proficiency.DEXTERITY),
		ProficiencyTrait("skill", 3, Proficiency.ANIMAL_HANDLING, Proficiency.ATHLETICS, Proficiency.INSIGHT, Proficiency.INVESTIGATION, Proficiency.NATURE, Proficiency.PERCEPTION, Proficiency.STEALTH, Proficiency.SURVIVAL),
		ProficiencyTrait("weapon", Proficiency.SIMPLE, Proficiency.MARTIAL),
		ProficiencyTrait("armour", Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR, Proficiency.SHIELDS)
	)
	override val multiclassTraits: List<Trait<*>> = listOf(
		ProficiencyTrait("skill", 1, Proficiency.ANIMAL_HANDLING, Proficiency.ATHLETICS, Proficiency.INSIGHT, Proficiency.INVESTIGATION, Proficiency.NATURE, Proficiency.PERCEPTION, Proficiency.STEALTH, Proficiency.SURVIVAL),
		ProficiencyTrait("weapon", Proficiency.MARTIAL),
		ProficiencyTrait("armour", Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR, Proficiency.SHIELDS)
	)
	override val features: List<List<Feature>> = listOf(

	)
}