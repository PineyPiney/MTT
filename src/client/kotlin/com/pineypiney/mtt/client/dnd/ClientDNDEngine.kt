package com.pineypiney.mtt.client.dnd

import com.pineypiney.mtt.client.dnd.network.ClientCharacter
import com.pineypiney.mtt.client.dnd.network.ClientCharacterNetworkHandler
import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import java.util.*

class ClientDNDEngine(val client: MinecraftClient) : DNDEngine<ClientCharacter>() {

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
			reset()
		}
	}

	fun onPlayerConnect() {

	}

	fun addCharactersToCombat(combatId: Int, characterUuids: Map<UUID, Int>) {
		val combat = combats.firstOrNull { it.id == combatId }
		val characters = characterUuids.mapKeys { (uuid, _) -> getCharacter(uuid)!! }
		if (combat != null) combat.enterCharacters(characters)
		else {
			startCombat(combatId, characters)
		}
	}

	fun removeCharactersFromCombat(combatId: Int, characterUuids: Collection<UUID>) {
		val combat = combats.firstOrNull { it.id == combatId } ?: return
		combat.exitCharacters(characterUuids)
	}

	override fun getControllingPlayer(character: UUID): PlayerEntity? {
		val playerUuid = playerCharacters.entries.firstOrNull { it.value == character }?.key ?: return null
		return if (client.player?.uuid == playerUuid) client.player
		else client.server?.playerManager?.getPlayer(playerUuid)
	}

	override fun associatePlayer(player: UUID, character: UUID) {
		super.associatePlayer(player, character)
		if (running && player == client.player?.uuid) {
			updateCameraEntity(getEntityFromPlayer(player) ?: client.player)
		}
	}

	override fun dissociatePlayer(player: UUID) {
		super.dissociatePlayer(player)
		updateCameraEntity(client.player)
	}

	private fun updateCameraEntity(entity: Entity?) {
		val oldEntity = client.cameraEntity
		if (oldEntity is ClientDNDEntity) {
			oldEntity.trackedPosition.pos = oldEntity.entityPos
		}
		client.cameraEntity = entity
	}

	override fun isClient(): Boolean = true

	companion object {
		fun getInstance() =
			(MinecraftClient.getInstance() as DNDEngineHolder<*>).`mtt$getDNDEngine`() as ClientDNDEngine

		fun getClientCharacter(): ClientCharacter? {
			val client = MinecraftClient.getInstance()
			val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`() as ClientDNDEngine
			return engine.getCharacterFromPlayer(client.player?.uuid ?: return null)
		}

		fun getClientCharacterUuid(): UUID? {
			val client = MinecraftClient.getInstance()
			val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`()
			return engine.getCharacterUuidFromPlayer(client.player?.uuid ?: return null)
		}

		fun getRunningAndPlayerCharacter(player: PlayerEntity?): Character? {
			val engine = getInstance()
			if (!engine.running) return null
			return engine.getCharacterFromPlayer(player?.uuid ?: return null)
		}

		fun getRunningAndPlayerCharacterEntity(player: PlayerEntity?): DNDEntity? {
			val engine = getInstance()
			if (!engine.running) return null
			val characterUuid = engine.getCharacterUuidFromPlayer(player?.uuid ?: return null) ?: return null
			return engine.getEntityOfCharacter(characterUuid)
		}
	}
}