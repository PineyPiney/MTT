package com.pineypiney.mtt.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.pineypiney.mtt.dnd.DamageType

data class DamageRolls(val types: List<DiceDamage>) {

	constructor(type: DamageType, num: Int, sides: Int, bonus: Int): this(listOf(DiceDamage(type, num, sides, bonus)))

	data class DiceDamage(val type: DamageType, val numDice: Int, val sides: Int, val bonus: Int){
		constructor(type: String, num: Int, sides: Int, bonus: Int): this(DamageType.find(type), num, sides, bonus)
		fun typeName() = type.name
	}

	companion object {
		val SINGLE_CODEC = RecordCodecBuilder.create { builder ->
			builder.group(
				Codec.STRING.optionalFieldOf("type", "slashing").forGetter(DiceDamage::typeName),
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