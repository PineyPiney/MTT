package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.dnd.CharacterSheet
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.text.Text

abstract class CharacterCreatorTabWidget(val sheet: CharacterSheet, val client: MinecraftClient, x: Int, var yOrigin: Int, width: Int, val panelHeight: Int, text: Text) : ContainerWidget(x, yOrigin, width, panelHeight, text){

	var yOffset = 0

	abstract fun reposition(start: Int = -1)

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}