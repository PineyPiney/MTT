package com.pineypiney.mtt

import com.pineypiney.mtt.dnd.DNDClientEngine
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.SpeciesS2CPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

class MTTClientNetwork {

	companion object {
		@Suppress("UNCHECKED_CAST")
		fun registerPayloads(){
			ClientPlayNetworking.registerGlobalReceiver(DNDEngineUpdateS2CPayload.ID){ payload, ctx ->
				val client = ctx.client() ?: return@registerGlobalReceiver
				val engine = (client as DNDEngineHolder<*>).dndEngine
				payload.apply(engine)
			}

			ClientPlayNetworking.registerGlobalReceiver(SpeciesS2CPayload.ID){ payload, ctx ->
				MTT.logger.info("Received details of DND species ${payload.species.id}")
				val client = ctx.client() ?: return@registerGlobalReceiver
				val engine = (client as DNDEngineHolder<DNDClientEngine>).dndEngine

				// If the game engine already received a species with the same name then replace the old one with the new one
				engine.receivedSpecies.removeIf { it.id == payload.species.id }
				engine.receivedSpecies.add(payload.species)
			}
		}
	}
}