package com.pineypiney.mtt.network

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.c2s.ClickButtonC2SPayload
import com.pineypiney.mtt.network.payloads.c2s.OpenDNDScreenC2SPayload
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.SpeciesS2CPayload
import com.pineypiney.mtt.screen.CharacterMakerScreenHandler
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class MTTNetwork {

	companion object {
		@Suppress("UNCHECKED_CAST")
		fun registerPayloads(){
			PayloadTypeRegistry.playS2C().register(DNDEngineUpdateS2CPayload.ID, DNDEngineUpdateS2CPayload.CODEC)
			PayloadTypeRegistry.playS2C().register(SpeciesS2CPayload.ID, SpeciesS2CPayload.CODEC)

			PayloadTypeRegistry.playC2S().register(OpenDNDScreenC2SPayload.ID, OpenDNDScreenC2SPayload.CODEC)
			PayloadTypeRegistry.playC2S().register(ClickButtonC2SPayload.ID, ClickButtonC2SPayload.CODEC)

			ServerPlayNetworking.registerGlobalReceiver(OpenDNDScreenC2SPayload.ID){ payload, ctx ->
				val engine = (ctx.server() as? DNDEngineHolder<DNDServerEngine>)?.dndEngine
				if(engine == null) return@registerGlobalReceiver
				val character = engine.getPlayer(ctx.player().uuid)
				if(character != null) ctx.player().openHandledScreen(character)
			}

			ServerPlayNetworking.registerGlobalReceiver(ClickButtonC2SPayload.ID){ payload, ctx ->
				val handler = (ctx.player().currentScreenHandler as? CharacterMakerScreenHandler) ?: return@registerGlobalReceiver
				val engine = (ctx.server() as? DNDEngineHolder<*>)?.dndEngine as? DNDServerEngine ?: return@registerGlobalReceiver

				when(payload.buttonType){
					"species" -> {
						val species = engine.loadedSpecies[payload.buttonID]
						if(species != null) handler.setSpecies(species)
					}
					"class" -> {
						val clazz = DNDClass.classes.firstOrNull { it.id == payload.buttonID }
						if(clazz != null) handler.setClass(clazz)
					}
					"background" -> {
						val background = Background.set.firstOrNull { it.id == payload.buttonID }
						if(background != null) handler.setBackground(background)
					}
				}
			}
		}
	}
}