package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DNDEngine
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.util.*

class DNDEngineUpdateS2CPayload(val field: String, val data: String) : CustomPayload {

	override fun getId(): CustomPayload.Id<out CustomPayload?>? = ID

	fun apply(engine: DNDEngine){
		when(field){
			"running" -> engine.running = data[0].code == 1
			"dm" -> {
				engine.DM = if(data.isEmpty()) null
				else try {UUID.fromString(data) } catch(_: IllegalArgumentException) { null }
			}
			"player" -> {
				val index = data.indexOf('/')
				val player = if(index == -1) data else data.substring(0, index)
				val playerUUID = try { UUID.fromString(player) } catch (_: IllegalArgumentException) { return }
				if(playerUUID == null) return

				if(index == -1) engine.dissociatePlayer(playerUUID)
				else {
					val character = data.substring(index + 1)
					val characterUUID = try { UUID.fromString(character) } catch (_: IllegalArgumentException) { null }
					if (characterUUID == null) engine.dissociatePlayer(playerUUID)
					else engine.associatePlayer(playerUUID, characterUUID)
				}
			}
		}
	}

	companion object {
		val ENGINE_UPDATE_PAYLOAD_ID = Identifier.of(MTT.Companion.MOD_ID, "engine")
		val ID = CustomPayload.Id<DNDEngineUpdateS2CPayload>(ENGINE_UPDATE_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(PacketCodecs.STRING, DNDEngineUpdateS2CPayload::field, PacketCodecs.STRING, DNDEngineUpdateS2CPayload::data, ::DNDEngineUpdateS2CPayload)
	}
}