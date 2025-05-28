package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.ProficiencyTrait
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.features.Features

object Barbarian : DNDClass("barbarian", 12) {

	override val coreTraits: List<Trait<*>> = listOf(
		ProficiencyTrait("ability", Proficiency.STRENGTH, Proficiency.CONSTITUTION),
		ProficiencyTrait("skill", 2, Proficiency.ANIMAL_HANDLING, Proficiency.ATHLETICS, Proficiency.INTIMIDATION, Proficiency.NATURE, Proficiency.PERCEPTION, Proficiency.SURVIVAL),
		ProficiencyTrait("weapon", Proficiency.SIMPLE, Proficiency.MARTIAL),
		ProficiencyTrait("armour", Proficiency.SHIELDS, Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR)
	)

	override val multiclassTraits: List<Trait<*>> = listOf(
		ProficiencyTrait("weapon", Proficiency.MARTIAL),
		ProficiencyTrait("armour", Proficiency.SHIELDS)
	)

	override val features: List<List<Feature>> = listOf(
		listOf(Features.Rage, Features.UnarmouredDefense(Ability.CONSTITUTION)),
		listOf(Features.RecklessAttack, Features.DangerSense)
	)
}