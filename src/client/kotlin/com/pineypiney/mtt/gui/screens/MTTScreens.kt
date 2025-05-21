package com.pineypiney.mtt.gui.screens

import com.pineypiney.mtt.gui.widget.CharacterMakerScreen
import com.pineypiney.mtt.screen.MTTScreenHandlers
import net.minecraft.client.gui.screen.ingame.HandledScreens

class MTTScreens {

	companion object {

		fun registerScreens(){
			HandledScreens.register(MTTScreenHandlers.DND_SCREEN_HANDLER, ::DNDScreen)
			HandledScreens.register(MTTScreenHandlers.CHARACTER_MAKER_SCREEN_HANDLER, ::CharacterMakerScreen)
		}
	}
}