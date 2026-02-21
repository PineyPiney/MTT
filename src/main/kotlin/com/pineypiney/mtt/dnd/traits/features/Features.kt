package com.pineypiney.mtt.dnd.traits.features

import com.pineypiney.mtt.dnd.characters.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import kotlin.math.max
import kotlin.random.Random

class Features {
	class AbilityScoreImprovement(val ability: Ability, val boost: Int) : Feature("ability_score_improvement"){
		override fun modifyAbility(sheet: CharacterSheet, ability: Ability, initialAbility: Int): Int {
			return if(ability == this.ability) initialAbility + boost
			else initialAbility
		}

		override fun encode(buf: ByteBuf) {
			CODEC.encode(buf, this)
		}

		companion object {
			val CODEC = PacketCodec.tuple(
				MTTPacketCodecs.ABILITY, AbilityScoreImprovement::ability,
				MTTPacketCodecs.bytInt, AbilityScoreImprovement::boost,
				::AbilityScoreImprovement
			)
		}
	}
	class UnarmouredDefense(val second: Ability) : Feature("unarmoured_defense_${second.id}") {
		override fun modifyArmour(sheet: CharacterSheet, initialArmourClass: Int): Int {
			return if(initialArmourClass > 10) initialArmourClass
			else initialArmourClass + sheet.abilities.dexMod + sheet.abilities.getMod(second)
		}

		override fun encode(buf: ByteBuf) {
			CODEC.encode(buf, this)
		}

		companion object {
			val CODEC = MTTPacketCodecs.ABILITY.xmap(::UnarmouredDefense, UnarmouredDefense::second)
		}
	}


	object Rage : Feature("rage"){
		override fun onCreateActions(sheet: CharacterSheet) {

		}
	}
	object RecklessAttack : Feature("reckless_attack"){
		override fun onAttackRoll(sheet: CharacterSheet, roll: Int, target: Int) {

		}
	}
	object DangerSense : Feature("danger_sense"){
		override fun onSavingThrow(sheet: CharacterSheet, ability: Ability, type: String, initialRoll: Int): Int {
			return if(ability != Ability.DEXTERITY) initialRoll
			else max(initialRoll, Random.nextInt(1, 20))
		}
	}
	object AdrenalineRush : Feature("adrenaline_rush"){
		override fun onCreateActions(sheet: CharacterSheet) {

		}
	}

	object RelentlessEndurance : Feature("relentless_endurance")

	companion object {

		val set = mutableSetOf<Feature>()
		val classCodecs = mutableMapOf<String, PacketCodec<ByteBuf, *>>()

		init {
			set.add(Rage)
			set.add(RecklessAttack)
			set.add(DangerSense)
			set.add(AdrenalineRush)
			set.add(RelentlessEndurance)

			classCodecs["ability_score_improvement"] = AbilityScoreImprovement.CODEC
			classCodecs["unarmoured_defense"] = UnarmouredDefense.CODEC
		}
	}
}