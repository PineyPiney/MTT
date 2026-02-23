package com.pineypiney.mtt.client

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

object MTTKeybinds {

	val category = KeyBinding.Category.create(Identifier.of(MTT.MOD_ID, "category.mtt"))

	val sheetScreenBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
		"key.mtt.character_sheet",
		GLFW.GLFW_KEY_C,
		category
	))

	fun registerKeyBindings(){
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			while(sheetScreenBinding.wasPressed()){
				val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`()
				val currentCharacter = engine.getCharacterFromPlayer(client.player?.uuid ?: continue) ?: continue
				val sheet = currentCharacter.details as? CharacterSheet ?: continue
				client.setScreen(CharacterSheetScreen(sheet, currentCharacter))
			}
		}
	}
}