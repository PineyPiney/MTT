package com.pineypiney.mtt.client

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.classes.Wizard
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.util.*

object MTTKeybinds {

	val category = KeyBinding.Category.create(Identifier.of(MTT.MOD_ID, "category.mtt"))

	val sheetScreenBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
		"key.mtt.character_sheet",
		GLFW.GLFW_KEY_C,
		category
	))
	val sheetTestScreenBinding = KeyBindingHelper.registerKeyBinding(KeyBinding(
		"key.mtt.character_sheet_test",
		GLFW.GLFW_KEY_N,
		category
	))

	fun registerKeyBindings(){
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			while(sheetScreenBinding.wasPressed()){
				val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`()
				val currentCharacter = engine.getCharacterFromPlayer(client.player?.uuid ?: continue)
				if(currentCharacter != null) client.setScreen(CharacterSheetScreen(currentCharacter))
			}
			while(sheetTestScreenBinding.wasPressed()){
				val sheet = CharacterSheet()
				sheet.name = "Test Character"
				sheet.race = Race.findById("human")
				sheet.maxHealth = 10
				sheet.health = 10
				sheet.classes[Wizard] = 2
				sheet.abilities.strength = 18
				sheet.abilities.dexterity = 17
				sheet.abilities.constitution = 15
				sheet.abilities.intelligence = 9
				sheet.abilities.wisdom = 10
				sheet.addProficiencies(setOf(Proficiency.ATHLETICS), Source.RaceSource(sheet.race))

				val engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`()
				client.setScreen(CharacterSheetScreen(SheetCharacter(sheet, UUID.randomUUID(), engine)))
			}
		}
	}
}