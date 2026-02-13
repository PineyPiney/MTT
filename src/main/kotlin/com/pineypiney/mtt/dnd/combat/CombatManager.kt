package com.pineypiney.mtt.dnd.combat

import com.pineypiney.mtt.dnd.characters.Character

class CombatManager(val id: Int) {
	val combatants = mutableListOf<Character>()
}