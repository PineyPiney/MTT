package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class DeleteCharacterS2CPayload(val uuid: UUID) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "delete_char")
		val ID = CustomPayload.Id<DeleteCharacterS2CPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.UUID_CODEC, DeleteCharacterS2CPayload::uuid,
			::DeleteCharacterS2CPayload
		)
	}
}