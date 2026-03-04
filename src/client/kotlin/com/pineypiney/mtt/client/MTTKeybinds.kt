package com.pineypiney.mtt.client

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.dnd.ClientDNDEngine
import com.pineypiney.mtt.client.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import com.pineypiney.mtt.network.payloads.c2s.EndTurnC2SPayload
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
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
	val endTurnBinding = KeyBindingHelper.registerKeyBinding(
		KeyBinding(
			"key.mtt.end_turn",
			GLFW.GLFW_KEY_N,
			category
		)
	)

	fun registerKeyBindings(){
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			val character = ClientDNDEngine.getClientCharacter() ?: return@register
			while(sheetScreenBinding.wasPressed()){
				val sheet = character.details as? CharacterSheet ?: break
				client.setScreen(CharacterSheetScreen(sheet, character))
			}
			while (endTurnBinding.wasPressed()) {
				ClientPlayNetworking.send(EndTurnC2SPayload(character.uuid))
			}
		}
	}
}