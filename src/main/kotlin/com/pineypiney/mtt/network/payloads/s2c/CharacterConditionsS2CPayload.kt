package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class CharacterConditionsS2CPayload(val character: UUID, val nbt: NbtCompound) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val CHARACTER_CONDITIONS_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "character_conditions")
		val ID = CustomPayload.Id<CharacterConditionsS2CPayload>(CHARACTER_CONDITIONS_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.UUID_CODEC, CharacterConditionsS2CPayload::character,
			PacketCodecs.NBT_COMPOUND, CharacterConditionsS2CPayload::nbt,
			::CharacterConditionsS2CPayload
		)
	}
}