package com.pineypiney.mtt.gui.widget.sheet_widgets

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.gui.widget.DynamicWidgets
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class HealthWidget(val screen: CharacterSheetScreen, val character: Character, x: Int, y: Int, w: Int, h: Int): ClickableWidget(x, y, w, h, Text.translatable("mtt.trait.health")) {

	val healthLabel = Text.translatable("mtt.trait.health")
	val healthWidth = screen.textRenderer.getWidth(healthLabel)
	val tempLabel = Text.translatable("mtt.trait.health.temp.abbr")
	val tempWidth = screen.textRenderer.getWidth(tempLabel)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		DynamicWidgets.drawRoundedBorder(context, x, y, width, height)
		context.drawText(screen.textRenderer, healthLabel, x + (83 - healthWidth) / 2, y + 24, 4210752, false)
		context.drawText(screen.textRenderer, tempLabel, x + 96 - (tempWidth / 2), y + 24, 4210752, false)

		val healthText = Text.literal("${character.health}/${character.maxHealth}")
		val healthTextW = screen.textRenderer.getWidth(healthText) - 1
		val tempText = Text.literal("0")
		val tempTextW = screen.textRenderer.getWidth(tempText) - 1

		context.matrices.push()
		context.matrices.scale(2f, 2f, 2f)
		val provider = context.vertexConsumers
		screen.textRenderer.draw(healthText, (x + 41.5f - healthTextW) * .5f, (y + 5) * .5f, 4210752, false, context.matrices.peek().positionMatrix, provider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
		screen.textRenderer.draw(tempText, (x + 96 - tempTextW) * .5f, (y + 5) * .5f, 4210752, false, context.matrices.peek().positionMatrix, provider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
		context.matrices.pop()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

	}

	override fun getTooltip(): Tooltip? {
		return super.getTooltip()
	}
}