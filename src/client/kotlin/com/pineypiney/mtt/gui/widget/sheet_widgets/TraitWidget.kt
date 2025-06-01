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

class TraitWidget(val screen: CharacterSheetScreen, val character: Character, traitKey: String, val labelY: Int, val statGetter: (Character) -> String, val statY: Int, x: Int, y: Int, w: Int, h: Int): ClickableWidget(x, y, w, h, Text.translatable(traitKey)) {

	val label = Text.translatable("$traitKey.abbr")
	val labelWidth = screen.textRenderer.getWidth(label)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		DynamicWidgets.drawRoundedBorder(context, x, y, 28, 35)
		context.drawText(screen.textRenderer, label, x + (width - labelWidth) / 2, y + labelY, 4210752, false)

		val valueString = statGetter(character)
		val valueText = Text.literal(valueString)
		val valueTextW = screen.textRenderer.getWidth(valueText) - 1

		context.matrices.push()
		context.matrices.scale(2f, 2f, 2f)
		val provider = context.vertexConsumers
		screen.textRenderer.draw(valueText, (x + width * .5f - valueTextW) * .5f, (y + statY) * .5f, 4210752, false, context.matrices.peek().positionMatrix, provider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
		context.matrices.pop()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

	}

	override fun getTooltip(): Tooltip? {
		return super.getTooltip()
	}
}