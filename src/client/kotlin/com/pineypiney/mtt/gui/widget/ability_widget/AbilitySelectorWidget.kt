package com.pineypiney.mtt.gui.widget.ability_widget

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.gui.widget.AbilitiesTabWidget
import com.pineypiney.mtt.network.payloads.c2s.UpdateTraitC2SPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

abstract class AbilitySelectorWidget(val tab: AbilitiesTabWidget, x: Int, y: Int, w: Int, h: Int, message: Text): ClickableWidget(x, y, w, h, message) {

	abstract val isReady: Boolean
	abstract val points: IntArray

	fun apply(sheet: CharacterSheet){
		sheet.abilities.strength = points[0]
		sheet.abilities.dexterity = points[1]
		sheet.abilities.constitution = points[2]
		sheet.abilities.intelligence = points[3]
		sheet.abilities.wisdom = points[4]
		sheet.abilities.charisma = points[5]
	}

	fun update(index: Int){
		ClientPlayNetworking.send(UpdateTraitC2SPayload("ability", index, points[index], emptyList()))
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}