package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodecs

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

		val CODEC = MTTPacketCodecs.from(PacketCodecs.STRING, Ability::name, Ability::valueOf)
	}
}