package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class CharacterInteractCharacterC2SPayload(val character: UUID) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	enum class Type {
		ATTACK
	}

	companion object {

		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "character_interact")
		val ID = CustomPayload.Id<CharacterInteractCharacterC2SPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.UUID_CODEC, CharacterInteractCharacterC2SPayload::character,
			::CharacterInteractCharacterC2SPayload
		)
	}
}