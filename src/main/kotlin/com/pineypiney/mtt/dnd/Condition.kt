package com.pineypiney.mtt.dnd

enum class Condition {
	BLINDED,
	CHARMED,
	DEAFENED,
	EXHAUSTION,
	FRIGHTENED,
	GRAPPLED,
	INCAPACITATED,
	INVISIBLE,
	PARALYSED,
	PETRIFIED,
	POISONED,
	PRONE,
	RESTRAINED,
	STUNNED,
	UNCONSCIOUS;

	val id get() = name.lowercase()

	companion object {
		fun getById(id: String) = valueOf(id.uppercase())
	}
}