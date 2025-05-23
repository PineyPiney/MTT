package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.CharacterSheet
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.text.Text

abstract class CharacterCreatorTabWidget(val sheet: CharacterSheet, val client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, text: Text) : ContainerWidget(x, y, width, height, text){

	var yOffset = 0
	abstract fun reposition(start: Int = -1)
}