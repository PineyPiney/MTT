package com.pineypiney.mtt.dnd.rolls

import com.pineypiney.mtt.dice.DieRoll
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.traits.Ability

class AbilityCheck(val ability: Ability, val tags: Set<String>) {

	constructor(ability: Ability, vararg tags: String) : this(ability, tags.toSet())

	fun hasTag(tag: String) = tags.contains(tag)

	fun roll(character: Character): Int {
		val roll = DieRoll(20)
		return roll(character, roll)
	}

	fun roll(character: Character, roll: DieRoll): Int {
		character.conditions.forEachState { conditionModify(it, roll) }
		var t = roll.roll() + character.abilities.getMod(ability)
		if (character.isProficientIn(ability)) t += character.getProficiencyBonus()
		return t
	}

	fun <S : Condition.ConditionState<S>> conditionModify(state: Condition.ConditionState<S>, roll: DieRoll) {
		@Suppress("UNCHECKED_CAST")
		state.condition.modifyAbilityCheck(state as S, this, roll)
	}

	companion object {
		val INITIATIVE = AbilityCheck(Ability.DEXTERITY, "initiative")
	}
}