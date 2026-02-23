package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class ExitCombatS2CPayload(val combatID: Int, val characters: List<UUID>) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "exit_combat")
		val ID = CustomPayload.Id<ExitCombatS2CPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.shtInt, ExitCombatS2CPayload::combatID,
			MTTPacketCodecs.smallCollection(MTTPacketCodecs.UUID_CODEC, ::List), ExitCombatS2CPayload::characters,
			::ExitCombatS2CPayload
		)
	}
}