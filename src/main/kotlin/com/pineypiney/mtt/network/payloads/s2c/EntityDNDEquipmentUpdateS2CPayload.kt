package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class EntityDNDEquipmentUpdateS2CPayload(val entityID: Int, val changes: Map<Int, ItemStack>) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {
		private val EQUIPMENT_UPDATE_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "equipment_changes")
		val ID = CustomPayload.Id<EntityDNDEquipmentUpdateS2CPayload>(EQUIPMENT_UPDATE_PAYLOAD_ID)
		val CODEC: PacketCodec<RegistryByteBuf, EntityDNDEquipmentUpdateS2CPayload> = PacketCodec.tuple(
			PacketCodecs.INTEGER, EntityDNDEquipmentUpdateS2CPayload::entityID,
			MTTPacketCodecs.map<RegistryByteBuf, Int, ItemStack>(MTTPacketCodecs.INT(), ItemStack.OPTIONAL_PACKET_CODEC), EntityDNDEquipmentUpdateS2CPayload::changes,
			::EntityDNDEquipmentUpdateS2CPayload
		)
	}
}