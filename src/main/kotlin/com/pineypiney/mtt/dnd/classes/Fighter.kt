package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitOption
import com.pineypiney.mtt.dnd.traits.features.Feature

object Fighter : DNDClass("fighter", 10) {
	override val coreTraits: List<Trait<*>> = listOf(
		SetTraits(CharacterSheet::addProficiencies, Proficiency.STRENGTH, Proficiency.CONSTITUTION),
		TraitOption(2, CharacterSheet::addProficiencies, Proficiency.ACROBATICS, Proficiency.ANIMAL_HANDLING, Proficiency.ATHLETICS, Proficiency.HISTORY, Proficiency.INSIGHT, Proficiency.INTIMIDATION, Proficiency.PERSUASION, Proficiency.PERCEPTION, Proficiency.SURVIVAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.SIMPLE, Proficiency.MARTIAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR, Proficiency.HEAVY_ARMOUR, Proficiency.SHIELDS)
	)
	override val multiclassTraits: List<Trait<*>> = listOf(
		SetTraits(CharacterSheet::addProficiencies, Proficiency.MARTIAL),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.LIGHT_ARMOUR, Proficiency.MEDIUM_ARMOUR, Proficiency.SHIELDS)
	)
	override val features: List<List<Feature>> = listOf(

	)
}