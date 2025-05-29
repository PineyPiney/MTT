package com.pineypiney.mtt.network

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.c2s.ClickButtonC2SPayload
import com.pineypiney.mtt.network.payloads.c2s.OpenDNDScreenC2SPayload
import com.pineypiney.mtt.network.payloads.c2s.UpdateTraitC2SPayload
import com.pineypiney.mtt.network.payloads.s2c.CharacterSheetS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.SpeciesS2CPayload
import com.pineypiney.mtt.screen.CharacterMakerScreenHandler
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class MTTNetwork {

	companion object {

		fun getEngine(ctx: ServerPlayNetworking.Context) = (ctx.server() as? DNDEngineHolder<*>)?.dndEngine as? DNDServerEngine
		@Suppress("UNCHECKED_CAST")
		fun registerPayloads() {
			PayloadTypeRegistry.playS2C().register(DNDEngineUpdateS2CPayload.ID, DNDEngineUpdateS2CPayload.CODEC)
			PayloadTypeRegistry.playS2C().register(SpeciesS2CPayload.ID, SpeciesS2CPayload.CODEC)
			PayloadTypeRegistry.playS2C().register(CharacterSheetS2CPayload.ID, CharacterSheetS2CPayload.CODEC)

			PayloadTypeRegistry.playC2S().register(OpenDNDScreenC2SPayload.ID, OpenDNDScreenC2SPayload.CODEC)
			PayloadTypeRegistry.playC2S().register(ClickButtonC2SPayload.ID, ClickButtonC2SPayload.CODEC)
			PayloadTypeRegistry.playC2S().register(UpdateTraitC2SPayload.ID, UpdateTraitC2SPayload.CODEC)

			ServerPlayNetworking.registerGlobalReceiver(OpenDNDScreenC2SPayload.ID) { payload, ctx ->
				val engine = getEngine(ctx)
				if (engine == null) return@registerGlobalReceiver
				val character = engine.getPlayer(ctx.player().uuid)
				if (character != null) ctx.player().openHandledScreen(character)
			}

			ServerPlayNetworking.registerGlobalReceiver(ClickButtonC2SPayload.ID) { payload, ctx ->
				val handler = (ctx.player().currentScreenHandler as? CharacterMakerScreenHandler) ?: return@registerGlobalReceiver

				when (payload.buttonType) {
					"species" -> {
						val species = Species.findById(payload.buttonID)
						handler.setSpecies(species)
					}

					"class" -> {
						val clazz = DNDClass.classes.firstOrNull { it.id == payload.buttonID }
						if (clazz != null) handler.setClass(clazz)
					}

					"background" -> {
						val background = Background.findById(payload.buttonID)
						handler.setBackground(background)
					}

					"sheet_maker" -> {
						handler.applyTraits()
						val engine = getEngine(ctx) ?: return@registerGlobalReceiver
						val character = SheetCharacter(handler.sheet)
						engine.characters.add(character)
						val uuid = ctx.player()?.uuid
						if(uuid != null) engine.playerCharacters[uuid] = character

						ctx.responseSender().sendPacket(CharacterSheetS2CPayload(handler.sheet, uuid))
					}
				}
			}


			ServerPlayNetworking.registerGlobalReceiver(UpdateTraitC2SPayload.ID) { payload, ctx ->
				val handler = (ctx.player().currentScreenHandler as? CharacterMakerScreenHandler) ?: return@registerGlobalReceiver
				handler.updateTrait(payload.source, payload.traitIndex, payload.partIndex, payload.decisions)
			}
		}
	}
}