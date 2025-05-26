package com.pineypiney.mtt.gui.widget.ability_widget

import com.pineypiney.mtt.gui.widget.AbilitiesTabWidget
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

abstract class AbilitySelectorWidget(val tab: AbilitiesTabWidget, x: Int, y: Int, w: Int, h: Int, message: Text): ClickableWidget(x, y, w, h, message) {

	val points = IntArray(6){ 8 }

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}