package com.pineypiney.mtt.dnd.network

import com.pineypiney.mtt.dice.DieRoll
import com.pineypiney.mtt.dice.RollResult
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.CharacterDetails
import com.pineypiney.mtt.dnd.rolls.AbilityCheck
import com.pineypiney.mtt.dnd.rolls.AttackRoll
import com.pineypiney.mtt.dnd.rolls.SavingThrow
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import java.util.*

class ServerCharacter(override val details: CharacterDetails, uuid: UUID, override val engine: ServerDNDEngine) : Character(uuid) {

	override fun attack(target: Character) {
		super.attack(target)
		val characterCombat = engine.getCombat(this)
		val otherCombat = engine.getCombat(target)

		if (characterCombat == null) {
			if (otherCombat == null) {
				engine.startCombat(this, target)
			} else otherCombat.enterCharacter(this)
		} else if (characterCombat != otherCombat) {
			if (otherCombat == null) characterCombat.enterCharacter(target)
			else {
				characterCombat.merge(otherCombat)
			}
		}
	}

	override fun rollSavingThrow(savingThrow: SavingThrow, target: Int, roll: DieRoll): RollResult {
		val result = super.rollSavingThrow(savingThrow, target, roll)
		engine.onCharacterRollDice(this, result, if (result >= target || result.crit) 1 else -1)
		return result
	}

	override fun rollAbilityCheck(check: AbilityCheck, target: Int, roll: DieRoll): RollResult {
		val result = super.rollAbilityCheck(check, target, roll)
		engine.onCharacterRollDice(this, result, if (result >= target || result.crit) 1 else -1)
		return result
	}

	override fun rollInitiative(roll: DieRoll): RollResult {
		val result = super.rollInitiative(roll)
		engine.onCharacterRollDice(this, result, 0)
		return result
	}

	override fun rollAttackRoll(roll: AttackRoll, target: Int, dieRoll: DieRoll): RollResult {
		val result = super.rollAttackRoll(roll, target, dieRoll)
		engine.onCharacterRollDice(this, result, if (result >= target || result.crit) 1 else -1)
		return result
	}
}