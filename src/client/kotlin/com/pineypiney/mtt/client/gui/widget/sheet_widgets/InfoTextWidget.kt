package com.pineypiney.mtt.client.gui.widget.sheet_widgets

import com.pineypiney.mtt.client.gui.screens.CharacterSheetScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class InfoTextWidget(
	val screen: CharacterSheetScreen,
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	text: Text,
	info: Text,
	val colour: Int = -12566464
) : ClickableWidget(x, y, width, height, text) {

	constructor(screen: CharacterSheetScreen, x: Int, y: Int, text: Text, info: Text, colour: Int = -12566464) : this(
		screen,
		x,
		y,
		screen.textRenderer.getWidth(text),
		screen.textRenderer.fontHeight,
		text,
		info,
		colour
	)

	init {
		setTooltip(Tooltip.of(info))
	}

	override fun renderWidget(
		context: DrawContext,
		mouseX: Int,
		mouseY: Int,
		deltaTicks: Float
	) {
		context.drawText(screen.textRenderer, message, x, y, colour, false)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
		builder.put(NarrationPart.TITLE, message)
	}
}