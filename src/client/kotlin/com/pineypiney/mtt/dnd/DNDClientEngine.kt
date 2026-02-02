package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.entity.DNDPlayerEntity
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
	override val playerEntities: List<DNDPlayerEntity> get() = client.player?.let { it.clientWorld?.getEntitiesByType(MTTEntities.PLAYER, Box.of(it.pos, 200.0, 200.0, 200.0)){ true } } ?: emptyList()

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

	companion object {
		fun getInstance() = (MinecraftClient.getInstance() as DNDEngineHolder<*>).`mtt$getDNDEngine`() as DNDClientEngine
		fun getClientCharacter(): SheetCharacter?{
			val client = MinecraftClient.getInstance()
			val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`()
			return engine.getPlayerCharacter(client.player?.uuid ?: return null)
		}
		fun getClientCharacterUUID(): UUID?{
			val client = MinecraftClient.getInstance()
			val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`()
			return engine.getPlayerCharacterUUID(client.player?.uuid ?: return null)
		}
		fun getRunningAndPlayerCharacter(player: PlayerEntity?): SheetCharacter? {
			val engine = getInstance()
			if(!engine.running) return null
			return engine.getPlayerCharacter(player?.uuid ?: return null)
		}
		fun getRunningAndPlayerCharacterEntity(player: PlayerEntity?): DNDPlayerEntity? {
			val engine = getInstance()
			if(!engine.running) return null
			val characterUUID = engine.getPlayerCharacterUUID(player?.uuid ?: return null) ?: return null
			return engine.getPlayerCharacterEntity(characterUUID)
		}
	}
}