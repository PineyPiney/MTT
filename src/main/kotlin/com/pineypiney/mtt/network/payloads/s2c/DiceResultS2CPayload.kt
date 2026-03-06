package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dice.RollResult
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class DiceResultS2CPayload(val id: Int, val list: List<RollResult>, val success: Int) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "dice_res")
		val ID = CustomPayload.Id<DiceResultS2CPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, DiceResultS2CPayload::id,
			MTTPacketCodecs.smallCollection(RollResult.CODEC, ::List), DiceResultS2CPayload::list,
			MTTPacketCodecs.bytInt, DiceResultS2CPayload::success,
			::DiceResultS2CPayload
		)
	}
}