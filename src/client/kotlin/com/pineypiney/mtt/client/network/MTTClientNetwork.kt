package com.pineypiney.mtt.client.network

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.dnd.DNDClientEngine
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.characters.SimpleCharacter
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.s2c.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object MTTClientNetwork {

	private fun getEngine(ctx: ClientPlayNetworking.Context) =
		(ctx.client() as? DNDEngineHolder<*>)?.`mtt$getDNDEngine`() as? DNDClientEngine

	@Suppress("UNCHECKED_CAST")
	fun registerPayloads() {
		ClientPlayNetworking.registerGlobalReceiver(DNDEngineUpdateS2CPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			when (payload.field) {
				"running" -> {
					engine.running = payload.data[0] == 1
					val player = ctx.client().player ?: return@registerGlobalReceiver

					if (engine.running) {
						val characterEntity = engine.getEntityFromPlayer(player.uuid)
						ctx.client().cameraEntity = characterEntity ?: player
					} else ctx.client().cameraEntity = player
				}

				"dm" -> {
					engine.DM = if (payload.data.size < 4) null
					else payload.getUUID(0)
				}

				"player" -> {
					if (payload.data.size < 4) return@registerGlobalReceiver
					val playerUUID = payload.getUUID(0)

					if (payload.data.size < 8) engine.dissociatePlayer(playerUUID)
					else {
						val characterUUID = try {
							payload.getUUID(4)
						} catch (_: IllegalArgumentException) {
							null
						}
						if (characterUUID == null) engine.dissociatePlayer(playerUUID)
						else engine.associatePlayer(playerUUID, characterUUID)
					}
				}

				"rename" -> {
					val characterUUID = payload.getUUID(0)
					val character = engine.getCharacter(characterUUID) ?: return@registerGlobalReceiver
					character.name = payload.string
				}
			}
		}

		ClientPlayNetworking.registerGlobalReceiver(RaceS2CPayload.ID) { payload, _ ->
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

		ClientPlayNetworking.registerGlobalReceiver(CharacterParamsS2CPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			val character = SimpleCharacter(payload.params, payload.uuid, engine)
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
		ClientPlayNetworking.registerGlobalReceiver(CharacterPositionLookS2CPayload.ID) { payload, ctx ->
			getEngine(ctx)?.networkHandler?.onPlayerPositionLook(payload)
		}
	}
}