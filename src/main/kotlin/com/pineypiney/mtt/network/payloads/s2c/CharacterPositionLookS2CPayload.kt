package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import net.minecraft.entity.EntityPosition
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.util.Identifier

class CharacterPositionLookS2CPayload(
	val teleportID: Int,
	val change: EntityPosition,
	val relatives: Set<PositionFlag>
) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {

		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "character_position_look")
		val ID = CustomPayload.Id<CharacterPositionLookS2CPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, CharacterPositionLookS2CPayload::teleportID,
			EntityPosition.PACKET_CODEC, CharacterPositionLookS2CPayload::change,
			PositionFlag.PACKET_CODEC, CharacterPositionLookS2CPayload::relatives,
			::CharacterPositionLookS2CPayload
		)
	}
}