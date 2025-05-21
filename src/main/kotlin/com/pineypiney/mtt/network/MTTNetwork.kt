package com.pineypiney.mtt.network

import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.c2s.OpenDNDScreenC2SPayload
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.SpeciesS2CPayload
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class MTTNetwork {

	companion object {
		@Suppress("UNCHECKED_CAST")
		fun registerPayloads(){
			PayloadTypeRegistry.playS2C().register(DNDEngineUpdateS2CPayload.ID, DNDEngineUpdateS2CPayload.CODEC)
			PayloadTypeRegistry.playS2C().register(SpeciesS2CPayload.ID, SpeciesS2CPayload.CODEC)

			PayloadTypeRegistry.playC2S().register(OpenDNDScreenC2SPayload.ID, OpenDNDScreenC2SPayload.CODEC)

			ServerPlayNetworking.registerGlobalReceiver(OpenDNDScreenC2SPayload.ID){ payload, ctx ->
				val engine = (ctx.server() as? DNDEngineHolder<DNDServerEngine>)?.dndEngine
				if(engine == null) return@registerGlobalReceiver
				val character = engine.getPlayer(ctx.player().uuid)
				if(character != null) ctx.player().openHandledScreen(character)
			}
		}
	}
}