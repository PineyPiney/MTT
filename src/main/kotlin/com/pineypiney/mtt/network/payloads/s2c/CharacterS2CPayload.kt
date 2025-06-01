package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class CharacterS2CPayload(val character: Character) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload?>? {
		return ID
	}

	companion object {
		val CHARACTER_PAYLOAD_ID = Identifier.of(MTT.Companion.MOD_ID, "sheet")
		val ID = CustomPayload.Id<CharacterS2CPayload>(CHARACTER_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.CHARACTER_CODEC, CharacterS2CPayload::character,
			::CharacterS2CPayload
		)
	}
}