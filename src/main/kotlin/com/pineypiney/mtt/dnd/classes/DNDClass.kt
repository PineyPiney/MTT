package com.pineypiney.mtt.dnd.classes

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.features.Feature

abstract class DNDClass(val id: String, val healthDie: Int) {

	abstract val coreTraits: List<Trait<*>>
	abstract val multiclassTraits: List<Trait<*>>
	abstract val features: List<List<Feature>>

	open fun onInitialClass(sheet: CharacterSheet){
		//for(trait in coreTraits) trait.applyWithValues(sheet, Source.ClassSource(this))
	}
	open fun onMultiClass(sheet: CharacterSheet){
		//for(trait in multiclassTraits) trait.applyWithValues(sheet, Source.ClassSource(this))
	}
	open fun onLevelUp(sheet: CharacterSheet, classLevel: Int){
		sheet.features[Source.ClassSource(this)]?.addAll(features[classLevel - 1])
	}

	companion object {
		val classes = listOf(Barbarian, Fighter, Ranger, Wizard)
	}
}