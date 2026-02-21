package com.pineypiney.mtt.dnd.conditions

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dice.DieRoll
import com.pineypiney.mtt.dnd.Duration
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.rolls.AbilityCheck
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.util.Identifier
import java.util.*

object Conditions {

	val map = mutableMapOf<Identifier, Condition<*>>()

	fun register(id: Identifier, condition: Condition<*>): Condition<*> {
		map[id] = condition
		return condition
	}

	open class BasicCondition(id: String) : Condition<BasicCondition.BasicState>(id) {
		override fun createState(buf: ByteBuf): BasicState = BasicState(this, MTTPacketCodecs.DURATION.decode(buf))

		fun createState(duration: Duration) = BasicState(this, duration)

		class BasicState(condition: Condition<BasicState>, duration: Duration) : ConditionState<BasicState>(condition, duration)
	}

	object BLINDED : BasicCondition("blinded") {
		override fun modifyAbilityCheck(state: BasicState, check: AbilityCheck, roll: DieRoll) {
			if (check.hasTag("sight")) roll.multiplier = 0f
		}

		override fun modifyAttackRoll(state: BasicState, roll: DieRoll) {
			roll.dis = true
		}

		override fun modifyAttackedRoll(state: BasicState, roll: DieRoll) {
			roll.adv = true
		}
	}

	object CHARMED : Condition<CHARMED.State>("charmed") {
		override fun createState(buf: ByteBuf): State = State(
			MTTPacketCodecs.UUID_CODEC.decode(buf),
			MTTPacketCodecs.DURATION.decode(buf)
		)

		override fun modifyAbilityCheck(state: State, check: AbilityCheck, roll: DieRoll) {
			if (check.hasTag("social")) roll.adv = true
		}

		override fun canTarget(state: State, target: Character): Boolean = target.uuid != state.charmer

		class State(val charmer: UUID, duration: Duration) : ConditionState<State>(CHARMED, duration) {
			override fun encode(buf: ByteBuf) {
				MTTPacketCodecs.UUID_CODEC.encode(buf, charmer)
				super.encode(buf)
			}
		}
	}

	object CHILL_TOUCH : BasicCondition("chill_touch") {
		override fun modifyHealing(state: BasicState, base: Int): Int = 0
	}

	object RAY_OF_FROST : BasicCondition("ray_of_frost") {
		override fun modifyWalkSpeed(state: BasicState, base: Int): Int = base - 10
	}

	object SHOCKING_GRASP : BasicCondition("ray_of_frost")

	object TRUE_STRIKE : Condition<TRUE_STRIKE.State>("true_strike") {
		override fun createState(buf: ByteBuf): State = State(MTTPacketCodecs.DURATION.decode(buf), MTTPacketCodecs.ABILITY.decode(buf), buf.readBoolean())

		class State(duration: Duration, val spellCastingAbility: Ability, val radiant: Boolean) : ConditionState<State>(TRUE_STRIKE, duration) {
			override fun encode(buf: ByteBuf) {
				super.encode(buf)
				MTTPacketCodecs.ABILITY.encode(buf, spellCastingAbility)
				buf.writeBoolean(radiant)
			}
		}
	}

	init {
		register(MTT.identifier("blinded"), BLINDED)
		register(MTT.identifier("charmed"), CHARMED)
		register(MTT.identifier("chill_touch"), CHILL_TOUCH)
		register(MTT.identifier("ray_of_frost"), RAY_OF_FROST)
		register(MTT.identifier("ray_of_frost"), SHOCKING_GRASP)
		register(MTT.identifier("true_strike"), TRUE_STRIKE)
	}

	fun findById(id: Identifier): Condition<*> = map[id] ?: throw IllegalArgumentException("No condition found with ID $id")

	fun forEach(action: (condition: Condition<*>) -> Unit) {
		map.values.forEach(action)
	}
}