package com.pineypiney.mtt.network

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.c2s.ClickButtonC2SPayload
import com.pineypiney.mtt.network.payloads.c2s.OpenDNDScreenC2SPayload
import com.pineypiney.mtt.network.payloads.c2s.UpdateTraitC2SPayload
import com.pineypiney.mtt.network.payloads.s2c.CharacterS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.EntityDNDEquipmentUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.RaceS2CPayload
import com.pineypiney.mtt.screen.CharacterMakerScreenHandler
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import java.util.*

object MTTNetwork {

	fun getEngine(ctx: ServerPlayNetworking.Context) = (ctx.server() as? DNDEngineHolder<*>)?.`mtt$getDNDEngine`() as? DNDServerEngine

	@Suppress("UNCHECKED_CAST")
	fun registerPayloads() {
		PayloadTypeRegistry.playS2C().register(DNDEngineUpdateS2CPayload.ID, DNDEngineUpdateS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(RaceS2CPayload.ID, RaceS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(CharacterS2CPayload.ID, CharacterS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(EntityDNDEquipmentUpdateS2CPayload.ID, EntityDNDEquipmentUpdateS2CPayload.CODEC)

		PayloadTypeRegistry.playC2S().register(OpenDNDScreenC2SPayload.ID, OpenDNDScreenC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(ClickButtonC2SPayload.ID, ClickButtonC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(UpdateTraitC2SPayload.ID, UpdateTraitC2SPayload.CODEC)

		ServerPlayNetworking.registerGlobalReceiver(OpenDNDScreenC2SPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			val character = engine.getPlayerCharacter(ctx.player().uuid)
			if (character != null) ctx.player().openHandledScreen(character)
		}

		ServerPlayNetworking.registerGlobalReceiver(ClickButtonC2SPayload.ID) { payload, ctx ->
			val handler = (ctx.player().currentScreenHandler as? CharacterMakerScreenHandler) ?: return@registerGlobalReceiver

			when (payload.buttonType) {
				"race" -> {
					val race = Race.findById(payload.buttonID)
					handler.setRace(race)
				}

				"class" -> {
					val clazz = DNDClass.classes.firstOrNull { it.id == payload.buttonID }
					if (clazz != null) handler.setClass(clazz)
				}

				"background" -> {
					val background = Background.findById(payload.buttonID)
					handler.setBackground(background)
				}

				// A player has finished making their character
				// and wants to save it to the character list
				"sheet_maker" -> {
					// Close the screen for the player
					ctx.player()?.closeHandledScreen()

					handler.applyTraits()
					val engine = getEngine(ctx) ?: return@registerGlobalReceiver

					// Add the new character to the engine
					val character = SheetCharacter(handler.sheet, UUID.randomUUID())
					engine.addCharacter(character)

					// Associate this player with their new character
					val uuid = ctx.player()?.uuid
					if(uuid != null) engine.associatePlayer(uuid, character.uuid)
				}
			}
		}


		ServerPlayNetworking.registerGlobalReceiver(UpdateTraitC2SPayload.ID) { payload, ctx ->
			val handler = (ctx.player().currentScreenHandler as? CharacterMakerScreenHandler) ?: return@registerGlobalReceiver
			handler.updateTrait(payload.source, payload.traitIndex, payload.partIndex, payload.decisions)
		}
	}
}