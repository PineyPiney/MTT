package com.pineypiney.mtt.dnd.rolls

import com.pineypiney.mtt.dice.DieRoll
import com.pineypiney.mtt.dice.RollResult
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.traits.Ability

data class SavingThrow(val ability: Ability) {

	fun roll(character: Character): RollResult {
		val roll = DieRoll.d20()
		return roll(character, roll)
	}

	fun roll(character: Character, roll: DieRoll): RollResult {
		character.conditions.forEachState { conditionModify(it, roll) }
		var t = roll.roll() + character.abilities.getMod(ability)
		if (character.isProficientIn(ability)) t += character.getProficiencyBonus()
		return t
	}

	fun <S : Condition.ConditionState<S>> conditionModify(state: Condition.ConditionState<S>, roll: DieRoll) {
		@Suppress("UNCHECKED_CAST")
		state.condition.modifySavingThrow(state as S, this, roll)
	}
}