package com.pineypiney.mtt.dnd.traits.features

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Ability
import kotlin.math.max
import kotlin.random.Random

class Features {
	class AbilityScoreImprovement(val ability: Ability, val boost: Int) : Feature("ability_score_improvement"){
		override fun modifyAbility(sheet: CharacterSheet, ability: Ability, initialAbility: Int): Int {
			return if(ability == this.ability) initialAbility + boost
			else initialAbility
		}
	}
	object Rage : Feature("rage"){
		override fun onCreateActions(sheet: CharacterSheet) {

		}
	}
	class UnarmouredDefense(val second: Ability) : Feature("unarmoured_defense"){
		override fun modifyArmour(sheet: CharacterSheet, initialArmourClass: Int): Int {
			return if(initialArmourClass > 10) initialArmourClass
			else initialArmourClass + sheet.abilities.dexMod + sheet.abilities.getMod(second)
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

	object RelentlessEndurance : Feature("relentless_endurance"){

	}
}