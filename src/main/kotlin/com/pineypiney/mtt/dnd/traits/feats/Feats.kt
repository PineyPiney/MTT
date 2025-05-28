package com.pineypiney.mtt.dnd.traits.feats

class Feats {


	companion object {
		val allFeats: MutableSet<Feat> = mutableSetOf()
		init {
			allFeats.addAll(setOf(None, Alert, ClericInitiate, DruidInitiate, WizardInitiate, SavageAttacker, Skilled))
		}
		fun getById(id: String) = allFeats.firstOrNull { it.id == id } ?: None
	}

	object None : Feat("none")
	object Alert : Feat("alert")
	object ClericInitiate : Feat("cleric_initiate")
	object DruidInitiate : Feat("druid_initiate")
	object WizardInitiate : Feat("wizard_initiate")
	object SavageAttacker : Feat("savage_attacker")
	object Skilled : Feat("skilled")
}