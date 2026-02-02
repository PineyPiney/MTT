package com.pineypiney.mtt

import com.pineypiney.mtt.dnd.DNDClientEngine
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.s2c.CharacterSheetS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.EntityDNDEquipmentUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.RaceS2CPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object MTTClientNetwork {

	fun getEngine(ctx: ClientPlayNetworking.Context) =
		(ctx.client() as? DNDEngineHolder<*>)?.`mtt$getDNDEngine`() as? DNDClientEngine

	@Suppress("UNCHECKED_CAST")
	fun registerPayloads() {
		ClientPlayNetworking.registerGlobalReceiver(DNDEngineUpdateS2CPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			payload.apply(engine)
		}

		ClientPlayNetworking.registerGlobalReceiver(RaceS2CPayload.ID) { payload, ctx ->
			MTT.logger.info("Received details of DND race ${payload.race.id}")

			// If the game engine already received a race with the same name then replace the old one with the new one
			Race.set.removeIf { it.id == payload.race.id }
			Race.set.add(payload.race)
		}

		ClientPlayNetworking.registerGlobalReceiver(CharacterSheetS2CPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			val character = SheetCharacter(payload.sheet, payload.uuid, engine)
			engine.addCharacter(character)
			val networkHandler = ctx.client().networkHandler ?: return@registerGlobalReceiver
			character.readNbt(payload.nbt, networkHandler.registryManager)
		}

		ClientPlayNetworking.registerGlobalReceiver(EntityDNDEquipmentUpdateS2CPayload.ID) { payload, ctx ->
			val entity =
				ctx.client().world?.getEntityById(payload.entityID) as? DNDEntity ?: return@registerGlobalReceiver
			val inventory = entity.character?.inventory ?: return@registerGlobalReceiver
			for ((slot, stack) in payload.changes) inventory.equipment[slot] = stack
		}
	}
}