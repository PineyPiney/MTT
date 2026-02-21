package com.pineypiney.mtt.dnd.traits

enum class Ability {
	STRENGTH,
	DEXTERITY,
	CONSTITUTION,
	INTELLIGENCE,
	WISDOM,
	CHARISMA;

	val id: String get() = name.lowercase()

	companion object {
		fun get(name: String): Ability? {
			return try { valueOf(name) }
			catch (e: IllegalArgumentException){ null }
		}
	}
}