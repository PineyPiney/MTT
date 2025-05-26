package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitOption
import com.pineypiney.mtt.dnd.traits.feats.Feat
import com.pineypiney.mtt.dnd.traits.feats.Feats

class Background(val id: String, val ability1: Ability,val ability2: Ability,val ability3: Ability, val feat: Feat, val skill1: Proficiency, val skill2: Proficiency, val tool: Trait<Proficiency>) {

	init {
		set.add(this)
	}

	companion object {

		val set = mutableSetOf<Background>()

		val ACOLYTE = Background(
			"acolyte",
			Ability.INTELLIGENCE,
			Ability.WISDOM,
			Ability.CHARISMA,
			Feats.ClericInitiate,
			Proficiency.INSIGHT,
			Proficiency.RELIGION,
			SetTraits(Proficiency.CALLIGRAPHERS_SUPPLIES, CharacterSheet::addProficiencies)
		)

		val CRIMINAL = Background(
			"criminal", Ability.DEXTERITY, Ability.CONSTITUTION, Ability.INTELLIGENCE, Feats.Alert, Proficiency.STEALTH,
			Proficiency.SLEIGHT_OF_HAND, SetTraits(Proficiency.THIEVES_TOOLS, CharacterSheet::addProficiencies)
		)

		val SAGE = Background(
			"sage",
			Ability.CONSTITUTION,
			Ability.INTELLIGENCE,
			Ability.WISDOM,
			Feats.WizardInitiate,
			Proficiency.ARCANA,
			Proficiency.HISTORY,
			SetTraits(Proficiency.CALLIGRAPHERS_SUPPLIES, CharacterSheet::addProficiencies)
		)

		val SOLDIER = Background(
			"soldier",
			Ability.STRENGTH,
			Ability.DEXTERITY,
			Ability.CONSTITUTION,
			Feats.SavageAttacker,
			Proficiency.ATHLETICS,
			Proficiency.INTIMIDATION,
			TraitOption(1, Proficiency.findByTag("game"), CharacterSheet::addProficiencies)
		)
	}
}