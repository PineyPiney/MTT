package com.pineypiney.mtt.network

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.network.ServerCharacter
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.mixin_interfaces.MTTServerPlayer
import com.pineypiney.mtt.network.payloads.c2s.*
import com.pineypiney.mtt.network.payloads.s2c.*
import com.pineypiney.mtt.screen.CharacterMakerScreenHandler
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.math.Vec3d
import java.util.*

object MTTNetwork {

	fun getEngine(ctx: ServerPlayNetworking.Context) =
		(ctx.server() as? DNDEngineHolder<*>)?.`mtt$getDNDEngine`() as? ServerDNDEngine

	@Suppress("UNCHECKED_CAST")
	fun registerPayloads() {
		PayloadTypeRegistry.playS2C().register(DNDEngineUpdateS2CPayload.ID, DNDEngineUpdateS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(RaceS2CPayload.ID, RaceS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(CharacterSheetS2CPayload.ID, CharacterSheetS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(CharacterParamsS2CPayload.ID, CharacterParamsS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(DeleteCharacterS2CPayload.ID, DeleteCharacterS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(EntityDNDEquipmentUpdateS2CPayload.ID, EntityDNDEquipmentUpdateS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(CharacterPositionLookS2CPayload.ID, CharacterPositionLookS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(CharacterDamageS2CPayload.ID, CharacterDamageS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(OpenDNDScreenS2CPayload.ID, OpenDNDScreenS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(CharacterConditionsS2CPayload.ID, CharacterConditionsS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(EnterCombatS2CPayload.ID, EnterCombatS2CPayload.CODEC)
		PayloadTypeRegistry.playS2C().register(ExitCombatS2CPayload.ID, ExitCombatS2CPayload.CODEC)

		PayloadTypeRegistry.playC2S().register(OpenDNDScreenC2SPayload.ID, OpenDNDScreenC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(ClickButtonC2SPayload.ID, ClickButtonC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(UpdateTraitC2SPayload.ID, UpdateTraitC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(UpdateSelectedDNDSlotC2SPayload.ID, UpdateSelectedDNDSlotC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(CharacterMoveC2SPayload.ID, CharacterMoveC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(TeleportConfirmC2SPayload.ID, TeleportConfirmC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(CharacterInteractCharacterC2SPayload.ID, CharacterInteractCharacterC2SPayload.CODEC)
		PayloadTypeRegistry.playC2S().register(CastSpellC2SPayload.ID, CastSpellC2SPayload.CODEC)




		ServerPlayNetworking.registerGlobalReceiver(OpenDNDScreenC2SPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			val character = engine.getCharacterFromPlayer(ctx.player().uuid)
			if (character != null) (ctx.player() as? MTTServerPlayer)?.`mTT$openCharacterInventory`(character)
		}

		ServerPlayNetworking.registerGlobalReceiver(ClickButtonC2SPayload.ID) { payload, ctx ->
			val handler = (ctx.player().currentScreenHandler as? CharacterMakerScreenHandler) ?: return@registerGlobalReceiver

			when (payload.buttonType) {
				"race" -> {
					val race = Race.findById(payload.buttonID)
					handler.setRace(race)
				}

				"subrace" -> {
					val race = handler.sheet.race
					val subrace = race.getSubrace(payload.buttonID)
					handler.setSubRace(subrace)
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
					ctx.player().closeHandledScreen()

					handler.applyTraits()
					val engine = getEngine(ctx) ?: return@registerGlobalReceiver

					// Add the new character to the engine
					val character = ServerCharacter(
						handler.sheet,
						UUID.randomUUID(),
						(ctx.server() as DNDEngineHolder<*>).`mtt$getDNDEngine`() as ServerDNDEngine,
					)
					engine.addCharacter(character)

					// Associate this player with their new character
					val uuid = ctx.player().uuid
					if(uuid != null) engine.associatePlayer(uuid, character.uuid)
				}
			}
		}


		ServerPlayNetworking.registerGlobalReceiver(UpdateTraitC2SPayload.ID) { payload, ctx ->
			val handler = (ctx.player().currentScreenHandler as? CharacterMakerScreenHandler) ?: return@registerGlobalReceiver
			handler.updateTrait(payload.source, payload.traitIndex, payload.partIndex, payload.decisions)
		}


		ServerPlayNetworking.registerGlobalReceiver(UpdateSelectedDNDSlotC2SPayload.ID) { payload, ctx ->
			val engine: DNDEngine<*> = (ctx.server() as DNDEngineHolder<*>).`mtt$getDNDEngine`()
			val character = engine.getCharacterFromPlayer(ctx.player().uuid)
			if (character != null) {
				val dndSlot: Int = payload.slot
				if (dndSlot < character.inventory.getHotbarSize()) character.inventory.selectedSlot = dndSlot
			}
		}

		ServerPlayNetworking.registerGlobalReceiver(CharacterMoveC2SPayload.ID) { payload, ctx ->
			if (payload.getX(0.0).isNaN() ||
				payload.getY(0.0).isNaN() ||
				payload.getZ(0.0).isNaN() ||
				!payload.getYaw(0.0f).isFinite() ||
				!payload.getPitch(0.0f).isFinite()
			) return@registerGlobalReceiver

			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			engine.characterManager[ctx.player().uuid]?.onPlayerMove(payload)
		}

		ServerPlayNetworking.registerGlobalReceiver(TeleportConfirmC2SPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			engine.characterManager[ctx.player().uuid]?.onTeleportConfirm(payload)
		}

		ServerPlayNetworking.registerGlobalReceiver(CharacterInteractCharacterC2SPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			val character = engine.getCharacterFromPlayer(ctx.player().uuid)
			val target = engine.getCharacter(payload.character) ?: return@registerGlobalReceiver
			character?.attack(target)
		}

		ServerPlayNetworking.registerGlobalReceiver(CastSpellC2SPayload.ID) { payload, ctx ->
			val engine = getEngine(ctx) ?: return@registerGlobalReceiver
			val character = engine.getCharacterFromPlayer(ctx.player().uuid) ?: return@registerGlobalReceiver
			for ((i, target) in payload.locations.withIndex()) {
				payload.spell.cast(character, Vec3d(target), payload.angles.getOrNull(i) ?: 0f, payload.level, Ability.INTELLIGENCE, engine.getCombat(character))
			}
		}
	}
}