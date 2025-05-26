package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitOption
import com.pineypiney.mtt.dnd.traits.features.Feature

object Ranger : DNDClass("ranger", 10) {
	override val coreTraits: List<Trait<*>> = listOf(
		SetTraits(CharacterSheet::addProficiencies, Proficiency.STRENGTH, Proficiency.DEXTERITY),
		TraitOption(3, CharacterSheet::addProficiencies, Proficiency.ANIMAL_HANDLING, Proficiency.ATHLETICS, Proficiency.INSIGHT, Proficiency.INVESTIGATION, Proficiency.NATURE, Proficiency.PERCEPTION, Proficiency.STEALTH, Proficiency.SURVIVAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.SIMPLE, Proficiency.MARTIAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR, Proficiency.SHIELDS)
	)
	override val multiclassTraits: List<Trait<*>> = listOf(
		TraitOption(1, CharacterSheet::addProficiencies, Proficiency.ANIMAL_HANDLING, Proficiency.ATHLETICS, Proficiency.INSIGHT, Proficiency.INVESTIGATION, Proficiency.NATURE, Proficiency.PERCEPTION, Proficiency.STEALTH, Proficiency.SURVIVAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.MARTIAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR, Proficiency.SHIELDS)
	)
	override val features: List<List<Feature>> = listOf(

	)
}