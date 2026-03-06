package com.pineypiney.mtt.dnd.server

import com.pineypiney.mtt.dnd.network.ServerCharacter
import com.pineypiney.mtt.dnd.network.ServerDNDEntity
import com.pineypiney.mtt.dnd.server.network.ServerCharacterNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

class CharacterManager(val engine: ServerDNDEngine) {
	val handlers = mutableMapOf<UUID, ServerCharacterNetworkHandler>()

	fun connectPlayer(player: ServerPlayerEntity) {
		val handler = ServerCharacterNetworkHandler(engine, player)
		handler.character = engine.getCharacter(player.uuid)
		handlers[player.uuid] = handler
	}

	fun updateControlling(player: ServerPlayerEntity) {
		val handler = handlers[player.uuid]
		if (handler == null) connectPlayer(player)
		else handler.character = engine.getCharacterFromPlayer(player.uuid)
	}

	fun disconnectPlayer(player: ServerPlayerEntity) {
		handlers.remove(player.uuid)
	}

	operator fun get(playerUuid: UUID) = handlers[playerUuid]
	operator fun get(character: ServerCharacter): ServerCharacterNetworkHandler? {
		val player = engine.getControllingPlayer(character.uuid) ?: return null
		return handlers[player.uuid]
	}

	operator fun get(entity: ServerDNDEntity): ServerCharacterNetworkHandler? {
		return get(entity.character ?: return null)
	}
}