package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.traits.ProficiencyTrait
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency

object Fighter : DNDClass("fighter", 10) {
	override val coreTraits: List<Trait<*>> = listOf(
		ProficiencyTrait("ability", Proficiency.STRENGTH, Proficiency.CONSTITUTION),
		ProficiencyTrait("skill", 2, Proficiency.ACROBATICS, Proficiency.ANIMAL_HANDLING, Proficiency.ATHLETICS, Proficiency.HISTORY, Proficiency.INSIGHT, Proficiency.INTIMIDATION, Proficiency.PERSUASION, Proficiency.PERCEPTION, Proficiency.SURVIVAL),
		ProficiencyTrait("weapon", Proficiency.SIMPLE, Proficiency.MARTIAL),
		ProficiencyTrait("armour", Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR, Proficiency.HEAVY_ARMOUR, Proficiency.SHIELDS)
	)
	override val multiclassTraits: List<Trait<*>> = listOf(
		ProficiencyTrait("weapon", Proficiency.MARTIAL),
		ProficiencyTrait("armour", Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR, Proficiency.SHIELDS)
	)
	override val features: List<List<Feature>> = listOf(

	)
}