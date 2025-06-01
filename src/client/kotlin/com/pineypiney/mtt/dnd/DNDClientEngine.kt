package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.entity.DNDPlayerEntity
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.Box

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

	companion object {
		fun getInstance() = (MinecraftClient.getInstance() as DNDEngineHolder<*>).`mtt$getDNDEngine`() as DNDClientEngine
	}
}