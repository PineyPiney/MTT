package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.util.toInts
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class DNDEngineUpdateS2CPayload(val field: String, val data: List<Int>, val string: String) : CustomPayload {

	constructor(field: String, vararg uuids: UUID): this(field, uuids.flatMap { it.toInts() }, "")

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	private fun getUUID(offset: Int): UUID {
		return UUID(
			data[0 + offset].toLong() shl 32 or (data[1 + offset].toLong() and 0xffffffff),
			data[2 + offset].toLong() shl 32 or (data[3 + offset].toLong() and 0xffffffff)
		)
	}

	fun apply(engine: DNDEngine){
		when(field) {
			"running" -> engine.running = data[0] == 1
			"dm" -> {
				engine.DM = if(data.size < 4) null
				else getUUID(0)
			}
			"player" -> {
				if(data.size < 4) return
				val playerUUID = getUUID(0)

				if(data.size < 8) engine.dissociatePlayer(playerUUID)
				else {
					val characterUUID = try { getUUID(4) } catch (_: IllegalArgumentException) { null }
					if (characterUUID == null) engine.dissociatePlayer(playerUUID)
					else engine.associatePlayer(playerUUID, characterUUID)
				}
			}
			"rename" -> {
				val characterUUID = getUUID(0)
				val character = engine.getCharacter(characterUUID) ?: return
				character.name = string
			}
		}
	}

	companion object {
		val ENGINE_UPDATE_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "engine")
		val ID = CustomPayload.Id<DNDEngineUpdateS2CPayload>(ENGINE_UPDATE_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(PacketCodecs.STRING, DNDEngineUpdateS2CPayload::field, MTTPacketCodecs.collection(PacketCodecs.INTEGER, ::List), DNDEngineUpdateS2CPayload::data, PacketCodecs.STRING, DNDEngineUpdateS2CPayload::string, ::DNDEngineUpdateS2CPayload)
	}
}