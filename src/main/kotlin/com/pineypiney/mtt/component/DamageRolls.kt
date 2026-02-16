package com.pineypiney.mtt.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.pineypiney.mtt.dnd.DamageType
import kotlin.random.Random

data class DamageRolls(val types: List<DiceDamage>) {

	constructor(type: DamageType, num: Int, sides: Int, bonus: Int): this(listOf(DiceDamage(type, num, sides, bonus)))

	constructor(type: DamageType, flat: Int) : this(listOf(DiceDamage(type, 0, 1, flat)))

	operator fun plus(other: DamageRolls): DamageRolls = DamageRolls(types + other.types)
	operator fun plus(other: DiceDamage): DamageRolls = DamageRolls(types + other)

	data class DiceDamage(
		val type: DamageType,
		val numDice: Int,
		val sides: Int,
		val bonus: Int,
		val versatile: Boolean = false
	) {

		constructor(type: String, num: Int, sides: Int, bonus: Int): this(DamageType.find(type), num, sides, bonus)

		fun typeId() = type.id

		fun roll(crit: Boolean = false, twoHanded: Boolean = false): Int {
			if (numDice == 0) return bonus
			var i = bonus
			val max = if (versatile && twoHanded) sides + 3 else sides + 1
			repeat(if (crit) numDice * 2 else numDice) { i += Random.nextInt(1, max) }
			return i
		}
	}

	companion object {
		val SINGLE_CODEC = RecordCodecBuilder.create { builder ->
			builder.group(
				Codec.STRING.optionalFieldOf("type", "slashing").forGetter(DiceDamage::typeId),
				Codec.INT.optionalFieldOf("numDie", 1).forGetter(DiceDamage::numDice),
				Codec.INT.fieldOf("sides").forGetter(DiceDamage::sides),
				Codec.INT.optionalFieldOf("bonus", 0).forGetter(DiceDamage::bonus)
			).apply(builder, ::DiceDamage)
		}

		val CODEC = RecordCodecBuilder.create { builder ->
			builder.group(
				Codec.list(SINGLE_CODEC, 1, DamageType.list.size).fieldOf("types").forGetter(DamageRolls::types)
			).apply(builder, ::DamageRolls)
		}
	}
}