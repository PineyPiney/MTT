package com.pineypiney.mtt.client.gui.screens

import com.pineypiney.mtt.screen.MTTScreenHandlers
import net.minecraft.client.gui.screen.ingame.HandledScreens

class MTTScreens {

	companion object {

		val textColour = -12566464

		fun registerScreens(){
			HandledScreens.register(MTTScreenHandlers.CHARACTER_MAKER_SCREEN_HANDLER, ::CharacterMakerScreen)
		}
	}
}