package com.pineypiney.mtt.client.gui.widget.sheet_widgets

import com.pineypiney.mtt.client.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.client.gui.widget.DynamicWidgets
import com.pineypiney.mtt.dnd.characters.Character
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class HealthWidget(
	val screen: CharacterSheetScreen,
	val character: Character,
	x: Int,
	y: Int,
	w: Int,
	h: Int,
	val textColour: Int = -12566464
) : ClickableWidget(x, y, w, h, Text.translatable("mtt.trait.health")) {

	val healthLabel = Text.translatable("mtt.health")
	val healthWidth = screen.textRenderer.getWidth(healthLabel)
	val tempLabel = Text.translatable("mtt.health.temp.abbr")
	val tempWidth = screen.textRenderer.getWidth(tempLabel)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		DynamicWidgets.drawRoundedBorder(context, x, y, width, height)
		context.drawText(screen.textRenderer, healthLabel, x + (83 - healthWidth) / 2, y + 24, textColour, false)
		context.drawText(screen.textRenderer, tempLabel, x + 96 - (tempWidth / 2), y + 24, textColour, false)

		val healthText = Text.literal("${character.health}/${character.maxHealth}")
		val healthTextW = screen.textRenderer.getWidth(healthText) - 1
		val tempText = Text.literal("0")
		val tempTextW = screen.textRenderer.getWidth(tempText) - 1

		context.matrices.pushMatrix()
		context.matrices.scale(2f, 2f)
		context.matrices.pushMatrix()
		context.matrices.translate((x + 41.5f - healthTextW) * .5f, (y + 5) * .5f)
		context.drawText(screen.textRenderer, healthText, 0, 0, textColour, false)
		context.matrices.popMatrix()

		context.matrices.pushMatrix()
		context.matrices.translate((x + 96 - tempTextW) * .5f, (y + 5) * .5f)
		context.drawText(screen.textRenderer, tempText, 0, 0, textColour, false)
		context.matrices.popMatrix()

//		screen.textRenderer.draw(healthText, (x + 41.5f - healthTextW) * .5f, (y + 5) * .5f, 4210752, false, context.matrices.peek().positionMatrix, provider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
//		screen.textRenderer.draw(tempText, (x + 96 - tempTextW) * .5f, (y + 5) * .5f, 4210752, false, context.matrices.peek().positionMatrix, provider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
		context.matrices.popMatrix()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

	}
}