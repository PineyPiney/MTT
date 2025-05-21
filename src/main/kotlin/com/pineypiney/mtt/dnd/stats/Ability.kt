package com.pineypiney.mtt.dnd.stats

enum class Ability {
	STRENGTH,
	DEXTERITY,
	CONSTITUTION,
	INTELLIGENCE,
	WISDOM,
	CHARISMA;

	companion object {
		fun get(name: String): Ability? {
			return try { valueOf(name) }
			catch (e: IllegalArgumentException){ null }
		}
	}
}