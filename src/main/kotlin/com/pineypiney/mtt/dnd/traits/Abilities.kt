package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.network.codec.MTTPacketCodecs.uBytInt
import io.netty.buffer.ByteBuf
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

	fun setValues(values: IntArray) {
		strength = values[0]
		dexterity = values[1]
		constitution = values[2]
		intelligence = values[3]
		wisdom = values[4]
		charisma = values[5]
	}

	fun setValues(other: Abilities) {
		strength = other.strength
		dexterity = other.dexterity
		constitution = other.constitution
		intelligence = other.intelligence
		wisdom = other.wisdom
		charisma = other.charisma
	}

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

	fun decode(buf: ByteBuf) {
		strength = uBytInt.decode(buf)
		dexterity = uBytInt.decode(buf)
		constitution = uBytInt.decode(buf)
		intelligence = uBytInt.decode(buf)
		wisdom = uBytInt.decode(buf)
		charisma = uBytInt.decode(buf)

		for((_, list) in modifications){
			list.clear()
			list.addAll(MTTPacketCodecs.SOURCE_LIST.decode(buf))
		}
	}

	fun encode(buf: ByteBuf) {
		uBytInt.encode(buf, strength)
		uBytInt.encode(buf, dexterity)
		uBytInt.encode(buf, constitution)
		uBytInt.encode(buf, intelligence)
		uBytInt.encode(buf, wisdom)
		uBytInt.encode(buf, charisma)

		for ((_, list) in modifications) MTTPacketCodecs.SOURCE_LIST.encode(buf, list)
	}
}