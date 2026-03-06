package com.pineypiney.mtt.dnd.rolls

import com.pineypiney.mtt.dice.DieRoll
import com.pineypiney.mtt.dice.RollResult
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.conditions.Condition
import net.minecraft.item.ItemStack

data class AttackRoll(val weapon: ItemStack) {

	fun roll(character: Character): RollResult {
		val roll = DieRoll.d20()
		return roll(character, roll)
	}

	fun roll(character: Character, roll: DieRoll): RollResult {
		character.conditions.forEachState { conditionModify(it, roll) }
		return roll.roll() + character.getAttackBonus(weapon)

	}

	fun <S : Condition.ConditionState<S>> conditionModify(state: Condition.ConditionState<S>, roll: DieRoll) {
		@Suppress("UNCHECKED_CAST")
		state.condition.modifyAttackRoll(state as S, this, roll)
	}
}