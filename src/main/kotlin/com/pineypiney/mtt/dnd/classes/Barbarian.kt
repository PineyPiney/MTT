package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitOption
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.features.Features

object Barbarian : DNDClass("barbarian", 12) {

	override val coreTraits: List<Trait<*>> = listOf(
		SetTraits(CharacterSheet::addProficiencies, Proficiency.STRENGTH, Proficiency.CONSTITUTION),
		TraitOption(2, CharacterSheet::addProficiencies, Proficiency.ANIMAL_HANDLING, Proficiency.ATHLETICS, Proficiency.INTIMIDATION, Proficiency.NATURE, Proficiency.PERCEPTION, Proficiency.SURVIVAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.SIMPLE, Proficiency.MARTIAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.SHIELDS, Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR)
	)

	override val multiclassTraits: List<Trait<*>> = listOf(
		SetTraits(CharacterSheet::addProficiencies, Proficiency.MARTIAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.SHIELDS)
	)

	override val features: List<List<Feature>> = listOf(
		listOf(Features.Rage, Features.UnarmouredDefense(Ability.CONSTITUTION)),
		listOf(Features.RecklessAttack, Features.DangerSense)
	)
}