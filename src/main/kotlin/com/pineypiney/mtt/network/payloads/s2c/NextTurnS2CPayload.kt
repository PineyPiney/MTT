package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class NextTurnS2CPayload(val combatID: Int, val character: Int) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "next_turn")
		val ID = CustomPayload.Id<NextTurnS2CPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.uShtInt, NextTurnS2CPayload::combatID,
			MTTPacketCodecs.uShtInt, NextTurnS2CPayload::character,
			::NextTurnS2CPayload
		)
	}
}