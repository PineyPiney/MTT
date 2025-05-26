package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class ClickButtonC2SPayload(val buttonType: String, val buttonID: String) : CustomPayload {
	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {
		val CLICK_BUTTON_PAYLOAD_ID = Identifier.of(MTT.Companion.MOD_ID, "click_button")
		val ID = CustomPayload.Id<ClickButtonC2SPayload>(CLICK_BUTTON_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(PacketCodecs.STRING, ClickButtonC2SPayload::buttonType, PacketCodecs.STRING, ClickButtonC2SPayload::buttonID, ::ClickButtonC2SPayload)
	}
}