package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitOption
import com.pineypiney.mtt.dnd.traits.features.Feature

object Wizard : DNDClass("wizard", 6) {
	override val coreTraits: List<Trait<*>> = listOf(
		SetTraits(CharacterSheet::addProficiencies, Proficiency.INTELLIGENCE, Proficiency.WISDOM),
		TraitOption(2, CharacterSheet::addProficiencies, Proficiency.ARCANA, Proficiency.HISTORY, Proficiency.INSIGHT, Proficiency.INVESTIGATION, Proficiency.MEDICINE, Proficiency.NATURE, Proficiency.RELIGION),
		SetTraits(CharacterSheet::addProficiencies, Proficiency.SIMPLE),
	)
	override val multiclassTraits: List<Trait<*>> = listOf()
	override val features: List<List<Feature>> = listOf(

	)
}