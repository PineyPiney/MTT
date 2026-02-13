package com.pineypiney.mtt.client.gui.widget.ability_widget

import com.pineypiney.mtt.client.gui.widget.AbilitiesTabWidget
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

abstract class AbilitySelectorWidget(val tab: AbilitiesTabWidget, x: Int, y: Int, w: Int, h: Int, message: Text): ClickableWidget(x, y, w, h, message) {

	abstract val isReady: Boolean
	abstract val points: IntArray

	fun apply(sheet: CharacterSheet){
		sheet.abilities.setValues(points)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}