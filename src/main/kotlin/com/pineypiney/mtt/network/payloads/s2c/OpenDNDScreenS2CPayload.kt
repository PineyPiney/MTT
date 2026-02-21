package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class OpenDNDScreenS2CPayload(val syncId: Int, val character: UUID) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		private val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "open_dnd_screen")
		val ID = CustomPayload.Id<OpenDNDScreenS2CPayload>(PAYLOAD_ID)
		val CODEC: PacketCodec<ByteBuf, OpenDNDScreenS2CPayload> = PacketCodec.tuple(
			PacketCodecs.VAR_INT, OpenDNDScreenS2CPayload::syncId,
			MTTPacketCodecs.UUID_CODEC, OpenDNDScreenS2CPayload::character,
			::OpenDNDScreenS2CPayload
		)
	}
}