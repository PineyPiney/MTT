package com.pineypiney.mtt.dnd.conditions

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dice.DieRoll
import com.pineypiney.mtt.dnd.Duration
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.rolls.AbilityCheck
import com.pineypiney.mtt.dnd.rolls.SavingThrow
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.util.Identifier

abstract class Condition<S : Condition.ConditionState<S>>(val id: Identifier) {

	constructor(id: String) : this(MTT.identifier(id))

	abstract fun createState(buf: ByteBuf): S

	open fun modifyAbilityCheck(state: S, check: AbilityCheck, roll: DieRoll) {}
	open fun modifySavingThrow(state: S, check: SavingThrow, roll: DieRoll) {}

	open fun modifyAttackRoll(state: S, roll: DieRoll) {}
	open fun modifyAttackedRoll(state: S, roll: DieRoll) {}

	open fun modifyHealing(state: S, base: Int) = base

	open fun modifyWalkSpeed(state: S, base: Int) = base

	open fun canTarget(state: S, target: Character) = true

	open fun getTranslationKey() = "mtt.condition.${id.path}"

	open class ConditionState<S : ConditionState<S>>(val condition: Condition<S>, val duration: Duration) {

		open fun encode(buf: ByteBuf) {
			MTTPacketCodecs.DURATION.encode(buf, duration)
		}

		override fun toString(): String {
			return "${condition.id.path.replaceFirstChar(Char::uppercase)}[duration=$duration]"
		}
	}
}