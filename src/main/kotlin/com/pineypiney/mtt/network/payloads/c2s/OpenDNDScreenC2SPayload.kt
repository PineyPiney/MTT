package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class OpenDNDScreenC2SPayload(val screenType: Int): CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {
		val OPEN_SCREEN_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "open_screen")
		val ID = CustomPayload.Id<OpenDNDScreenC2SPayload>(OPEN_SCREEN_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, OpenDNDScreenC2SPayload::screenType, ::OpenDNDScreenC2SPayload)
	}
}