package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class RaceS2CPayload(val race: Race) : CustomPayload {
	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		private val RACE_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "race")
		val ID = CustomPayload.Id<RaceS2CPayload>(RACE_PAYLOAD_ID)
		val CODEC: PacketCodec<ByteBuf, RaceS2CPayload> =
			PacketCodec.tuple(MTTPacketCodecs.RACE, RaceS2CPayload::race, ::RaceS2CPayload)
	}
}