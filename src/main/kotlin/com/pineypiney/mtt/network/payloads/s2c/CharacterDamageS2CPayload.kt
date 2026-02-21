package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DamageType
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class CharacterDamageS2CPayload(val character: UUID, val type: DamageType, val amount: Int) : CustomPayload {
	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		private val CHARACTER_DAMAGE_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "character_damage")
		val ID = CustomPayload.Id<CharacterDamageS2CPayload>(CHARACTER_DAMAGE_PAYLOAD_ID)
		val CODEC: PacketCodec<ByteBuf, CharacterDamageS2CPayload> =
			PacketCodec.tuple(
				MTTPacketCodecs.UUID_CODEC, CharacterDamageS2CPayload::character,
				MTTPacketCodecs.DAMAGE_TYPE, CharacterDamageS2CPayload::type,
				PacketCodecs.INTEGER, CharacterDamageS2CPayload::amount,
				::CharacterDamageS2CPayload
			)
	}
}