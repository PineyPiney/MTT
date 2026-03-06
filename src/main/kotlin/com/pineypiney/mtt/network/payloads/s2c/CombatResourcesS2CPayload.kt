package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class CombatResourcesS2CPayload(val combatID: Int, val resources: Int) : CustomPayload {

	constructor(combatID: Int, actions: Int, bonusActions: Int, extraAttacks: Int) : this(combatID, compressInts(actions, bonusActions, extraAttacks))

	fun getActions() = resources and 3
	fun getBonusActions() = ((resources and 12) shl 2)
	fun getExtraAttacks() = ((resources and 48) shl 4)

	override fun getId(): CustomPayload.Id<out CustomPayload> {
		return ID
	}

	companion object {
		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "com_res")
		val ID = CustomPayload.Id<CombatResourcesS2CPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.uShtInt, CombatResourcesS2CPayload::combatID,
			MTTPacketCodecs.uBytInt, CombatResourcesS2CPayload::resources,
			::CombatResourcesS2CPayload
		)

		fun compressInts(actions: Int, bonusActions: Int, extraAttacks: Int): Int {
			return (actions and 3) + ((bonusActions and 3) shl 2) + ((extraAttacks and 3) shl 4)
		}
	}
}