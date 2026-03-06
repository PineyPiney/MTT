package com.pineypiney.mtt.dnd.combat

import com.pineypiney.mtt.dnd.characters.Character
import kotlin.math.max

class CombatResources {

	var totalMovement: Double = 30.0; private set
	var movementUsed: Double = 0.0; private set
	var actions: Int = 1; private set
	var bonusActions: Int = 1; private set
	var extraAttacks: Int = 0; private set

	fun startTurn(character: Character) {
		totalMovement = character.speed.toDouble()
		movementUsed = 0.0
		actions = 1
		bonusActions = 1
		extraAttacks = 0
	}

	fun travel(distance: Double) {
		movementUsed += distance
	}

	fun getMovementLeft() = max(totalMovement - movementUsed, 0.0)

	fun useAction(): Boolean {
		if (actions > 0) {
			actions--
			return true
		} else return false
	}
}