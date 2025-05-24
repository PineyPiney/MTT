package com.pineypiney.mtt.dnd.stats

class Abilities {
	var strength: Int = 10
	var dexterity: Int = 10
	var constitution: Int = 10
	var intelligence: Int = 10
	var wisdom: Int = 10
	var charisma: Int = 10

	operator fun get(ability: Ability) = getStat(ability)

	fun getStat(ability: Ability) = when(ability){
		Ability.STRENGTH -> strength
		Ability.DEXTERITY -> dexterity
		Ability.CONSTITUTION -> constitution
		Ability.INTELLIGENCE -> intelligence
		Ability.WISDOM -> wisdom
		Ability.CHARISMA -> charisma
	}

	val strMod get() = (strength - 10).floorDiv(2)
	val dexMod get() = (dexterity - 10).floorDiv(2)
	val conMod get() = (constitution - 10).floorDiv(2)
	val intMod get() = (intelligence - 10).floorDiv(2)
	val wisMod get() = (wisdom - 10).floorDiv(2)
	val chaMod get() = (charisma - 10).floorDiv(2)
	fun getMod(ability: Ability) = (getStat(ability) - 10).floorDiv(2)
}