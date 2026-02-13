package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class TeleportConfirmC2SPayload(val teleportID: Int) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {

		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "character_teleport_confirm")
		val ID = CustomPayload.Id<TeleportConfirmC2SPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER, TeleportConfirmC2SPayload::teleportID,
			::TeleportConfirmC2SPayload
		)
	}
}