package com.pineypiney.mtt.gui.widget

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text

class OptionsSelectWidget<T>(val textRenderer: TextRenderer, title: Text, currentSelection: List<T?>, val options: List<T>, val getText: (T) -> Text, x: Int, midY: Int, w: Int, h: Int, message: Text, val delete: (selected: List<T>) -> Unit) : ContainerWidget(x, midY, w, h, message) {

	val titleLines = textRenderer.wrapLines(title, width - 12)
	val texts = options.map { getText(it) }

	val numChoices = currentSelection.size
	val selectedOptions: MutableList<T> = currentSelection.filterNotNull().toMutableList()

	init {
		height = options.size * 12 + titleLines.size * 9 + 59
		y = midY - (height / 2)
	}


	override fun children(): List<Element> {
		return emptyList()
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		DynamicWidgets.drawThickBox(context, x, y, width, height, -3750202, -1, -11184811, RenderLayer.getGuiOverlay())
		context.enableScissor(x + 6, y + 6, x + width - 6, y + height - 6)

		var y = y + 6
		for(line in titleLines) {
			val lineWidth = textRenderer.getWidth(line)
			context.drawText(textRenderer, line, x + (width - lineWidth) / 2, y, 4210752, false)
			y += 9
		}
		y += 6

		val numSelectedText = Text.literal("${selectedOptions.size}/$numChoices")
		val lineWidth = textRenderer.getWidth(numSelectedText)
		context.drawText(textRenderer, numSelectedText, x + (width - lineWidth) / 2, y, 4210752, false)
		y+=15

		for(i in 0..<texts.size){
			val entry = texts[i]
			val backColour = if(selectedOptions.contains(options[i])) -6052957 else -3750202
			DynamicWidgets.drawThinBox(context, x + 11, y, 64, 12, backColour, -1, -9605779)
			context.drawText(textRenderer, entry, x + 14, y + 2, 4210752, false)
			y += 12
		}

		y += 9
		val doneText = Text.translatable("gui.done")
		DynamicWidgets.drawThinBox(context, x + 11, y, 64, 12, -3750202, -1, -9605779)
		context.drawText(textRenderer, doneText, x + (width - textRenderer.getWidth(doneText)) / 2, y + 2, 4210752, false)

		context.disableScissor()
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		val choicesY = y + 27 + titleLines.size * 9
		if(mouseX >= x + 11 && mouseY >= choicesY && mouseX < x + width - 11){
			val hoveredChoice = (mouseY.toInt() - choicesY) / 12
			if(hoveredChoice < options.size){
				val option = options[hoveredChoice]
				// If the hovered option is already selected then deselect it
				if(selectedOptions.contains(option)) selectedOptions.remove(option)
				else {
					// If not all choices have been made then add this to the selection
					if(selectedOptions.size < numChoices) selectedOptions.add(option)
					// Or if only one choice can be made then replace it with this one
					else if(numChoices == 1){
						selectedOptions[0] = option
					}
				}
				return true
			}
			val doneY = choicesY + options.size * 12 + 9
			if(mouseY >= doneY && mouseY < doneY + 12){
				delete(selectedOptions)
				return true
			}
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun getContentsHeightWithPadding(): Int {
		return options.size * 12
	}

	override fun getDeltaYPerScroll(): Double {
		return 15.0
	}

	override fun mouseScrolled(
		mouseX: Double,
		mouseY: Double,
		horizontalAmount: Double,
		verticalAmount: Double
	): Boolean {
		return false
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}// "vb gfdff": Harley 2025