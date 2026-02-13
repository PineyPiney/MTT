package com.pineypiney.mtt.client

import com.pineypiney.mtt.client.gui.MTTTooltips
import com.pineypiney.mtt.client.gui.screens.MTTScreens
import com.pineypiney.mtt.client.network.MTTClientNetwork
import com.pineypiney.mtt.client.render.MTTRenderers
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
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
		fun getClientCharacter(): SheetCharacter? {
			val client = MinecraftClient.getInstance() ?: return null
			val dndEngine = (client as? DNDEngineHolder<*>)?.`mtt$getDNDEngine`() ?: return null
			return dndEngine.getCharacterFromPlayer(client.player?.uuid ?: return null)
		}
	}
}