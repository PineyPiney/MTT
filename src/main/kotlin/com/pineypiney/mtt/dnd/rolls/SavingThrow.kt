package com.pineypiney.mtt.dnd.rolls

import com.pineypiney.mtt.dice.DieRoll
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.traits.Ability

data class SavingThrow(val ability: Ability, val target: Int) {

	fun roll(character: Character): Boolean {
		val roll = DieRoll(20)
		return roll(character, roll)
	}

	fun roll(character: Character, roll: DieRoll): Boolean {
		character.conditions.forEachState { conditionModify(it, roll) }
		var t = roll.roll() + character.abilities.getMod(ability)
		if (character.isProficientIn(ability)) t += character.getProficiencyBonus()
		return t >= target
	}

	fun <S : Condition.ConditionState<S>> conditionModify(state: Condition.ConditionState<S>, roll: DieRoll) {
		@Suppress("UNCHECKED_CAST")
		state.condition.modifySavingThrow(state as S, this, roll)
	}
}