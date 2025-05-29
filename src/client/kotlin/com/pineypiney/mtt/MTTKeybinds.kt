package com.pineypiney.mtt

import com.pineypiney.mtt.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW

object MTTKeybinds {
	val sheetScreenBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
		"key.mtt.character_sheet",
		GLFW.GLFW_KEY_C,
		"category.mtt"
	))

	fun registerKeyBindings(){
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			while(sheetScreenBinding.wasPressed()){
				val engine = (client as DNDEngineHolder<*>).dndEngine
				val currentCharacter = engine.playerCharacters[client.player?.uuid]
				if(currentCharacter != null) client.setScreen(CharacterSheetScreen(currentCharacter.sheet))
			}
		}
	}
}