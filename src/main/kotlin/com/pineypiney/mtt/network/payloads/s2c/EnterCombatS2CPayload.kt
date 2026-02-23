package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class EnterCombatS2CPayload(val combatID: Int, val characters: Map<UUID, Int>) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "enter_combat")
		val ID = CustomPayload.Id<EnterCombatS2CPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.shtInt, EnterCombatS2CPayload::combatID,
			MTTPacketCodecs.smallMap(MTTPacketCodecs.UUID_CODEC, MTTPacketCodecs.bytInt), EnterCombatS2CPayload::characters,
			::EnterCombatS2CPayload
		)
	}
}