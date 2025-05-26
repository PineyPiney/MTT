package com.pineypiney.mtt.dnd.traits

import java.util.*

class Abilities {
	var strength: Int = 10
	var dexterity: Int = 10
	var constitution: Int = 10
	var intelligence: Int = 10
	var wisdom: Int = 10
	var charisma: Int = 10

	var modifications = EnumMap<Ability, MutableList<Pair<Int, Source>>>(mapOf(
		Ability.STRENGTH to mutableListOf(),
		Ability.DEXTERITY to mutableListOf(),
		Ability.CONSTITUTION to mutableListOf(),
		Ability.INTELLIGENCE to mutableListOf(),
		Ability.WISDOM to mutableListOf(),
		Ability.CHARISMA to mutableListOf()
	))

	fun modify(ability: Ability, value: Int, src: Source){
		val list = modifications[ability]!!
		val existingSrc = list.firstOrNull { it.second == src }
		if(existingSrc == null) list.add(value to src)
		else {
			list.remove(existingSrc)
			list.add((value + existingSrc.first) to src)
		}
	}

	fun removeSrc(src: Source){
		for((_, list) in modifications){
			list.removeIf { it.second == src }
		}
	}

	operator fun get(ability: Ability) = getStat(ability)

	fun getStat(ability: Ability) = when(ability){
		Ability.STRENGTH -> strength
		Ability.DEXTERITY -> dexterity
		Ability.CONSTITUTION -> constitution
		Ability.INTELLIGENCE -> intelligence
		Ability.WISDOM -> wisdom
		Ability.CHARISMA -> charisma
	} + modifications[ability]!!.sumOf { it.first }

	val strMod get() = getMod(Ability.STRENGTH)
	val dexMod get() = getMod(Ability.DEXTERITY)
	val conMod get() = getMod(Ability.CONSTITUTION)
	val intMod get() = getMod(Ability.INTELLIGENCE)
	val wisMod get() = getMod(Ability.WISDOM)
	val chaMod get() = getMod(Ability.CHARISMA)
	fun getMod(ability: Ability) = (getStat(ability) - 10).floorDiv(2)
}