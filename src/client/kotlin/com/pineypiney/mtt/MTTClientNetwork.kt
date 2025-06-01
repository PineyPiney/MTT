package com.pineypiney.mtt

import com.pineypiney.mtt.dnd.DNDClientEngine
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.s2c.CharacterS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.EntityDNDEquipmentUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.SpeciesS2CPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

class MTTClientNetwork {

	companion object {

		fun getEngine(ctx: ClientPlayNetworking.Context) = (ctx.client() as? DNDEngineHolder<*>)?.`mtt$getDNDEngine`() as? DNDClientEngine
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

			ClientPlayNetworking.registerGlobalReceiver(CharacterS2CPayload.ID){ payload, ctx ->
				val engine = getEngine(ctx) ?: return@registerGlobalReceiver
				engine.addCharacter(payload.character)
			}

			ClientPlayNetworking.registerGlobalReceiver(EntityDNDEquipmentUpdateS2CPayload.ID){ payload, ctx ->
				val entity = ctx.client().world?.getEntityById(payload.entityID) as? DNDEntity ?: return@registerGlobalReceiver
				for((slot, stack) in payload.changes) entity.character.inventory.equipment[slot] = stack
			}
		}
	}
}