package com.pineypiney.mtt

import com.pineypiney.mtt.entity.DNDPlayerEntity
import com.pineypiney.mtt.gui.MTTTooltips
import com.pineypiney.mtt.gui.screens.MTTScreens
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.render.MTTRenderers
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient

class MTTClient : ClientModInitializer {
	override fun onInitializeClient() {
		MTTClientNetwork.registerPayloads()
		MTTScreens.registerScreens()
		MTTTooltips.registerTooltips()
		MTTRenderers.registerRenderers()
		MTTKeybinds.registerKeyBindings()
	}

	companion object {
		fun getClientCharacter(): DNDPlayerEntity? {
			val client = MinecraftClient.getInstance() ?: return null
			val dndEngine = (client as? DNDEngineHolder<*>)?.dndEngine ?: return null
			return dndEngine.getPlayer(client.player?.uuid ?: return null)
		}
	}
}