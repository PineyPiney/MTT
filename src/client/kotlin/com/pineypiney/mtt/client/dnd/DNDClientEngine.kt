package com.pineypiney.mtt.client.dnd

import com.pineypiney.mtt.client.dnd.network.ClientCharacterNetworkHandler
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import java.util.*

class DNDClientEngine(val client: MinecraftClient) : DNDEngine() {

	// On the client side the only players that we have to care about are the ones
	// in the same dimension as the client and in close proximity (100 blocks here)
	override val playerEntities: List<DNDEntity>
		get() = client.player?.let {
			it.entityWorld?.getEntitiesByType(
				MTTEntities.DND_ENTITY, Box.of(it.entityPos, 200.0, 200.0, 200.0)
			) { true }
		} ?: emptyList()

	val networkHandler = ClientCharacterNetworkHandler(this)

	init {
		// reset the client in case it joins a different server before closing the client
		ClientPlayConnectionEvents.DISCONNECT.register { handler, client ->
			running = false
			DM = null
			characters.clear()
			playerCharacters.clear()
		}
	}

	override fun getControllingPlayer(character: UUID): PlayerEntity? {
		val playerUUID = playerCharacters.entries.firstOrNull { it.value == character }?.key ?: return null
		return if (client.player?.uuid == playerUUID) client.player
		else client.server?.playerManager?.getPlayer(playerUUID)
	}

	override fun associatePlayer(player: UUID, character: UUID) {
		super.associatePlayer(player, character)
		if (running && player == client.player?.uuid) {
			val characterEntity = getEntityFromPlayer(player)
			client.cameraEntity = characterEntity ?: client.player
		}
	}

	override fun dissociatePlayer(player: UUID) {
		super.dissociatePlayer(player)
		client.cameraEntity = client.player
	}

	companion object {
		fun getInstance() =
			(MinecraftClient.getInstance() as DNDEngineHolder<*>).`mtt$getDNDEngine`() as DNDClientEngine

		fun getClientCharacter(): SheetCharacter? {
			val client = MinecraftClient.getInstance()
			val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`()
			return engine.getCharacterFromPlayer(client.player?.uuid ?: return null)
		}

		fun getClientCharacterUUID(): UUID? {
			val client = MinecraftClient.getInstance()
			val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`()
			return engine.getCharacterUUIDFromPlayer(client.player?.uuid ?: return null)
		}

		fun getRunningAndPlayerCharacter(player: PlayerEntity?): SheetCharacter? {
			val engine = getInstance()
			if (!engine.running) return null
			return engine.getCharacterFromPlayer(player?.uuid ?: return null)
		}

		fun getRunningAndPlayerCharacterEntity(player: PlayerEntity?): DNDEntity? {
			val engine = getInstance()
			if (!engine.running) return null
			val characterUUID = engine.getCharacterUUIDFromPlayer(player?.uuid ?: return null) ?: return null
			return engine.getEntityOfCharacter(characterUUID)
		}
	}
}