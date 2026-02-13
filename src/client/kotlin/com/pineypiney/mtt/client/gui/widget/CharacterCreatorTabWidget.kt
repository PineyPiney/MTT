package com.pineypiney.mtt.client.gui.widget

import com.pineypiney.mtt.client.gui.screens.MTTScreens
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.text.Text

abstract class CharacterCreatorTabWidget(val sheet: CharacterSheet, val client: MinecraftClient, x: Int, var yOrigin: Int, width: Int, val panelHeight: Int, text: Text) : ContainerWidget(x, yOrigin, width, panelHeight, text){

	abstract val isReady: Boolean
	var shouldShowDone = false

	open val headerHeight = 25
	val footerHeight get() = if(shouldShowDone) 24 else 0

	abstract fun reposition(start: Int = -1)
	abstract fun apply(sheet: CharacterSheet)

	fun renderTitle(context: DrawContext, titleKey: String) {
		val titleText = Text.translatable(titleKey)
		val titleWidth = client.textRenderer.getWidth(titleText)
		val s = 2.5f
		val titleX = (x + (width - titleWidth * s) * .5f)
		val titleY = y + 5
		context.matrices.pushMatrix()
		context.matrices.scale(s, s)
		context.drawText(
			client.textRenderer,
			titleText,
			(titleX / s).toInt(),
			(titleY / s).toInt(),
			MTTScreens.textColour,
			false
		)
		context.matrices.popMatrix()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}