package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class UpdateTraitC2SPayload(val source: String, val traitIndex: Int, val partIndex: Int, val decisions: List<String>) : CustomPayload {
	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {
		val UPDATE_TRAIT_PAYLOAD_ID = Identifier.of(MTT.Companion.MOD_ID, "update_trait")
		val ID = CustomPayload.Id<UpdateTraitC2SPayload>(UPDATE_TRAIT_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			PacketCodecs.STRING, UpdateTraitC2SPayload::source,
			PacketCodecs.INTEGER, UpdateTraitC2SPayload::traitIndex,
			PacketCodecs.INTEGER, UpdateTraitC2SPayload::partIndex,
			MTTPacketCodecs.collection(PacketCodecs.STRING, ::List), UpdateTraitC2SPayload::decisions,
			::UpdateTraitC2SPayload
		)
	}
}