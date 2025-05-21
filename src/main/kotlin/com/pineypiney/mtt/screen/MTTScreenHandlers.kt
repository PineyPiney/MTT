package com.pineypiney.mtt.screen

import com.pineypiney.mtt.MTT
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier

class MTTScreenHandlers {

	companion object {
		val DND_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MTT.MOD_ID, "dnd_screen"),
			ScreenHandlerType(::DNDScreenHandler, FeatureSet.empty()))

		val CHARACTER_MAKER_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MTT.MOD_ID, "character_maker_screen"),
			ScreenHandlerType(::CharacterMakerScreenHandler, FeatureSet.empty()))
	}
}