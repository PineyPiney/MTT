package com.pineypiney.mtt.serialisation

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.*

class MTTCodecs {

	companion object {
		val UUID_CODEC: Codec<UUID> = RecordCodecBuilder.create { builder ->
			builder.group(
				Codec.LONG.fieldOf("most").forGetter(UUID::getMostSignificantBits),
				Codec.LONG.fieldOf("least").forGetter(UUID::getLeastSignificantBits)
			).apply(builder, ::UUID)
		}
	}
}