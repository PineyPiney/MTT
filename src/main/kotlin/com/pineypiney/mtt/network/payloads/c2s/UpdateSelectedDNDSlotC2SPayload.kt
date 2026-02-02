package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class UpdateSelectedDNDSlotC2SPayload(val slot: Int) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {
		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "update_selected_slot")
		val ID = CustomPayload.Id<UpdateSelectedDNDSlotC2SPayload>(PAYLOAD_ID)
		val CODEC = PacketCodecs.INTEGER.xmap(::UpdateSelectedDNDSlotC2SPayload, UpdateSelectedDNDSlotC2SPayload::slot)
	}
}