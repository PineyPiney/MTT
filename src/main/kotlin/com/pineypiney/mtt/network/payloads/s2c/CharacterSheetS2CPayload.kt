package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.util.optional
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class CharacterSheetS2CPayload(val sheet: CharacterSheet, val associatedPlayer: Optional<UUID>) : CustomPayload {

	constructor(sheet: CharacterSheet, associatedPlayer: UUID?): this(sheet, associatedPlayer.optional())

	override fun getId(): CustomPayload.Id<out CustomPayload?>? {
		return ID
	}

	companion object {
		val CHARACTER_SHEET_PAYLOAD_ID = Identifier.of(MTT.Companion.MOD_ID, "sheet")
		val ID = CustomPayload.Id<CharacterSheetS2CPayload>(CHARACTER_SHEET_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.CHARACTER_SHEET_CODEC, CharacterSheetS2CPayload::sheet,
			PacketCodecs.optional(MTTPacketCodecs.UUID_CODEC), CharacterSheetS2CPayload::associatedPlayer,
			::CharacterSheetS2CPayload
		)
	}
}