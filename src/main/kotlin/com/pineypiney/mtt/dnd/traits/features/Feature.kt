package com.pineypiney.mtt.dnd.traits.features

import com.pineypiney.mtt.CharacterSheet
import com.pineypiney.mtt.dnd.stats.Ability

open class Feature(val id: String) {
	open fun onCreateActions(sheet: CharacterSheet){}
	open fun onShortRest(sheet: CharacterSheet){}
	open fun onLongRest(sheet: CharacterSheet){}
	open fun onSavingThrow(sheet: CharacterSheet, ability: Ability, type: String, initialRoll: Int): Int = initialRoll
	open fun onAttackRoll(sheet: CharacterSheet, roll: Int, target: Int){}
	open fun modifyArmour(sheet: CharacterSheet, initialArmourClass: Int): Int = initialArmourClass
}