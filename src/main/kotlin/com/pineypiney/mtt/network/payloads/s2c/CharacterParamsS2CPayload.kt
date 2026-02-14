package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.characters.SimpleCharacter
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class CharacterParamsS2CPayload(val uuid: UUID, val params: SimpleCharacter.Params, val nbt: NbtCompound) :
	CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val CHARACTER_SHEET_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "params")
		val ID = CustomPayload.Id<CharacterParamsS2CPayload>(CHARACTER_SHEET_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.UUID_CODEC, CharacterParamsS2CPayload::uuid,
			SimpleCharacter.Params.PACKET_CODEC, CharacterParamsS2CPayload::params,
			PacketCodecs.NBT_COMPOUND, CharacterParamsS2CPayload::nbt,
			::CharacterParamsS2CPayload
		)
	}
}