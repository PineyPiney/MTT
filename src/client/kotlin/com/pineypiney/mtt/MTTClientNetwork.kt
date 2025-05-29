package com.pineypiney.mtt

import com.pineypiney.mtt.dnd.DNDClientEngine
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.s2c.CharacterSheetS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.SpeciesS2CPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

class MTTClientNetwork {

	companion object {

		fun getEngine(ctx: ClientPlayNetworking.Context) = (ctx.client() as? DNDEngineHolder<*>)?.dndEngine as? DNDClientEngine
		@Suppress("UNCHECKED_CAST")
		fun registerPayloads(){
			ClientPlayNetworking.registerGlobalReceiver(DNDEngineUpdateS2CPayload.ID){ payload, ctx ->
				val engine = getEngine(ctx) ?: return@registerGlobalReceiver
				payload.apply(engine)
			}

			ClientPlayNetworking.registerGlobalReceiver(SpeciesS2CPayload.ID){ payload, ctx ->
				MTT.logger.info("Received details of DND species ${payload.species.id}")

				// If the game engine already received a species with the same name then replace the old one with the new one
				Species.set.removeIf { it.id == payload.species.id }
				Species.set.add(payload.species)
			}

			ClientPlayNetworking.registerGlobalReceiver(CharacterSheetS2CPayload.ID){ payload, ctx ->
				val engine = getEngine(ctx) ?: return@registerGlobalReceiver
				val character = SheetCharacter(payload.sheet)
				engine.characters.add(character)
				if(payload.associatedPlayer.isPresent) engine.playerCharacters[payload.associatedPlayer.get()] = character
			}
		}
	}
}