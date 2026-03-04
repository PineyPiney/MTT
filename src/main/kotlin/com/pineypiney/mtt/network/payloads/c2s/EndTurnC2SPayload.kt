package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class EndTurnC2SPayload(val character: UUID) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {
		val PAYLOAD_ID: Identifier = Identifier.of(MTT.MOD_ID, "end_turn")
		val ID = CustomPayload.Id<EndTurnC2SPayload>(PAYLOAD_ID)
		val CODEC = MTTPacketCodecs.UUID_CODEC.xmap(::EndTurnC2SPayload, EndTurnC2SPayload::character)
	}
}