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
			"dm" -> engine.DM = UUID.fromString(data)
			//"added player" -> engine.addPlayer(data, )
			//"removed player" -> engine.removePlayer(data)
			//"set controlling" -> {
			//	val (character, uuid) = data.split(';')
			//	engine.getPlayer(character)?.controllingPlayer = UUID.fromString(uuid)
			//}
		}
	}

	companion object {
		val ENGINE_UPDATE_PAYLOAD_ID = Identifier.of(MTT.Companion.MOD_ID, "engine")
		val ID = CustomPayload.Id<DNDEngineUpdateS2CPayload>(ENGINE_UPDATE_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(PacketCodecs.STRING, DNDEngineUpdateS2CPayload::field, PacketCodecs.STRING, DNDEngineUpdateS2CPayload::data, ::DNDEngineUpdateS2CPayload)
	}
}