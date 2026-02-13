package com.pineypiney.mtt.client.gui.widget.sheet_widgets

import com.pineypiney.mtt.client.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.client.gui.widget.DynamicWidgets
import com.pineypiney.mtt.dnd.characters.Character
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

class TraitWidget(
	val screen: CharacterSheetScreen,
	val character: Character,
	traitKey: String,
	val labelY: Int,
	val statGetter: (Character) -> String,
	val statY: Int,
	x: Int,
	y: Int,
	w: Int,
	h: Int,
	val textColour: Int = -12566464
) : ClickableWidget(x, y, w, h, Text.translatable(traitKey)) {

	val label = Text.translatable("$traitKey.abbr")
	val labelWidth = screen.textRenderer.getWidth(label)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		DynamicWidgets.drawRoundedBorder(context, x, y, 28, 35)
		context.drawText(screen.textRenderer, label, x + (width - labelWidth) / 2, y + labelY, textColour, false)

		val valueString = statGetter(character)
		val valueText = Text.literal(valueString)
		val valueTextW = screen.textRenderer.getWidth(valueText) - 1

		context.matrices.pushMatrix()
		context.matrices.scale(2f, 2f)
		context.matrices.translate((x + width * .5f - valueTextW) * .5f, (y + statY) * .5f)
		context.drawText(screen.textRenderer, valueText, 0, 0, textColour, false)
//		screen.textRenderer.draw(valueText, 0, 0, 4210752, false, context.matrices.peek().positionMatrix, provider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
		context.matrices.popMatrix()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

	}
}