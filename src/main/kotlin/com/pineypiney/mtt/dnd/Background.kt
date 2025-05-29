package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.traits.*
import com.pineypiney.mtt.dnd.traits.feats.Feat
import com.pineypiney.mtt.dnd.traits.feats.Feats
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency

class Background(val id: String, val ability1: Ability,val ability2: Ability,val ability3: Ability, val feat: Feat, val skill1: Proficiency, val skill2: Proficiency, val tool: ProficiencyTrait) {

	init {
		set.add(this)
	}

	fun compileTraits(): Set<Trait<*>>{
		return setOf(
			ProficiencyTrait("skill", skill1, skill2),
			tool,
			FeatTrait(setOf(feat)),
			AbilityImprovementTrait(setOf(ability1, ability2, ability3), 3),
		)
	}

	companion object {

		val set = mutableSetOf<Background>()

		fun findById(id: String) = set.firstOrNull { it.id == id } ?: NONE

		val NONE = Background(
			"none",
			Ability.STRENGTH,
			Ability.STRENGTH,
			Ability.STRENGTH,
			Feats.None,
			Proficiency.NONE,
			Proficiency.NONE,
			ProficiencyTrait("tool", Proficiency.NONE)
		)
		val ACOLYTE = Background(
			"acolyte",
			Ability.INTELLIGENCE,
			Ability.WISDOM,
			Ability.CHARISMA,
			Feats.ClericInitiate,
			Proficiency.INSIGHT,
			Proficiency.RELIGION,
			ProficiencyTrait("tool", Proficiency.CALLIGRAPHERS_SUPPLIES)
		)

		val CRIMINAL = Background(
			"criminal", Ability.DEXTERITY, Ability.CONSTITUTION, Ability.INTELLIGENCE, Feats.Alert, Proficiency.STEALTH,
			Proficiency.SLEIGHT_OF_HAND, ProficiencyTrait("tool", Proficiency.THIEVES_TOOLS)
		)

		val SAGE = Background(
			"sage",
			Ability.CONSTITUTION,
			Ability.INTELLIGENCE,
			Ability.WISDOM,
			Feats.WizardInitiate,
			Proficiency.ARCANA,
			Proficiency.HISTORY,
			ProficiencyTrait("tool", Proficiency.CALLIGRAPHERS_SUPPLIES)
		)

		val SOLDIER = Background(
			"soldier",
			Ability.STRENGTH,
			Ability.DEXTERITY,
			Ability.CONSTITUTION,
			Feats.SavageAttacker,
			Proficiency.ATHLETICS,
			Proficiency.INTIMIDATION,
			ProficiencyTrait("tool", 1, Proficiency.findByTag("game").toSet())
		)
	}
}