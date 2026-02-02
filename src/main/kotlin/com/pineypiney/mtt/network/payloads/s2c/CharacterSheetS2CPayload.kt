package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class CharacterSheetS2CPayload(val uuid: UUID, val sheet: CharacterSheet, val nbt: NbtCompound) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val CHARACTER_SHEET_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "sheet")
		val ID = CustomPayload.Id<CharacterSheetS2CPayload>(CHARACTER_SHEET_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.UUID_CODEC, CharacterSheetS2CPayload::uuid,
			MTTPacketCodecs.CHARACTER_SHEET, CharacterSheetS2CPayload::sheet,
			PacketCodecs.NBT_COMPOUND, CharacterSheetS2CPayload::nbt,
			::CharacterSheetS2CPayload
		)
	}
}