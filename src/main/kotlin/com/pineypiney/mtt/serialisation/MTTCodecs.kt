package com.pineypiney.mtt.serialisation

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.*
import kotlin.math.min

object MTTCodecs {


	fun createBoolByte(vararg values: Boolean): Int {
		var b = 0
		for (i in 0..<min(values.size, 8)) {
			b = b or if (values[i]) 1 shl i else 0
		}
		return b
	}

	val UUID_CODEC: Codec<UUID> = RecordCodecBuilder.create { builder ->
		builder.group(
			Codec.LONG.fieldOf("most").forGetter(UUID::getMostSignificantBits),
			Codec.LONG.fieldOf("least").forGetter(UUID::getLeastSignificantBits)
		).apply(builder, ::UUID)
	}
}