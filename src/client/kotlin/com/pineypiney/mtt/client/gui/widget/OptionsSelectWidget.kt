package com.pineypiney.mtt.client.gui.widget

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import kotlin.math.max
import kotlin.math.min

class OptionsSelectWidget<T>(
	val textRenderer: TextRenderer,
	title: Text,
	currentSelection: List<T>,
	val numChoices: Int,
	val options: List<T>,
	val getText: (T) -> Text,
	x: Int,
	midY: Int,
	w: Int,
	h: Int,
	message: Text,
	val textColour: Int = -12566464,
	val delete: (selected: List<T>) -> Unit
) : ContainerWidget(x, midY, w, h, message) {

	val titleLines = textRenderer.wrapLines(title, width - 12)
	val texts = options.map { getText(it) }

	val selectedOptions: MutableList<T> = currentSelection.filterNotNull().toMutableList()

	init {
		height = min(options.size * 12 + titleLines.size * 9 + 59, 216)
		reposition(midY)
	}

	val headerSize = 27 + 9 * titleLines.size
	val scrollingHeight = height - 32 - headerSize

	override fun children(): List<Element> {
		return emptyList()
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		DynamicWidgets.drawThickBoxWithBorder(context, x, y, width, height)
		context.enableScissor(x + 6, y + 6, x + width - 6, y + height - 6)

		var y = y + 6
		for(line in titleLines) {
			val lineWidth = textRenderer.getWidth(line)
			context.drawText(textRenderer, line, x + (width - lineWidth) / 2, y, textColour, false)
			y += 9
		}
		y += 6

		val numSelectedText = Text.literal("${selectedOptions.size}/$numChoices")
		val lineWidth = textRenderer.getWidth(numSelectedText)
		context.drawText(textRenderer, numSelectedText, x + (width - lineWidth) / 2, y, textColour, false)
		y+=15

		val optionsY = y
		val optionsB = this.y + height - 32
		val scrolls = overflows()
		context.enableScissor(x + 11, y, x + width - 11, optionsB)
		y = y - scrollY.toInt()
		val width = if(scrolls) width - 42 else width - 22
		for(i in 0..<texts.size){
			if(y > optionsY - 12) {
				val entry = texts[i]
				val backColour = if (selectedOptions.contains(options[i])) -6052957 else -3750202
				DynamicWidgets.drawThinBox(context, x + 11, y, width, 12, backColour, -1, -9605779)
				context.drawText(textRenderer, entry, x + 14, y + 2, textColour, false)
			}
			y += 12
			if(y > optionsB) break
		}
		drawScrollbar(context, mouseX, mouseY)
		context.disableScissor()

		val doneText = Text.translatable("gui.done")
		DynamicWidgets.drawThinBox(context, x + 11, optionsB + 9, this.width - 22, 12, -3750202, -1, -9605779)
		context.drawText(
			textRenderer,
			doneText,
			x + (this.width - textRenderer.getWidth(doneText)) / 2,
			optionsB + 11,
			textColour,
			false
		)

		context.disableScissor()
	}

	override fun drawScrollbar(context: DrawContext, mouseX: Int, mouseY: Int) {
		if(overflows()){
			DynamicWidgets.drawThinBox(context, x + width - 25, y + headerSize, 14, scrollingHeight, -7631989, -13158601, -1)
			DynamicWidgets.drawScroller(context, x + width - 24, scrollbarThumbY + 1, 12, scrollbarThumbHeight - 2)
		}
	}

	override fun mouseClicked(click: Click, double: Boolean): Boolean {
		val choicesY = y + headerSize - scrollY.toInt()
		if (click.x >= x + 11 && click.y >= y + headerSize && click.x < x + width - 11) {
			val hoveredChoice = (click.y.toInt() - choicesY) / 12
			if (click.y < y + height - 32 && hoveredChoice < options.size) {
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
			val doneY = y + height - 23
			if (click.y >= doneY && click.y < doneY + 12) {
				delete(selectedOptions)
				return true
			}
		}
		return super.mouseClicked(click, double)
	}

	fun reposition(midY: Int){
		y = midY - min(height / 2, 94)
	}

	override fun getMaxScrollY(): Int {
		return max(0, contentsHeightWithPadding - scrollingHeight)
	}

	override fun getScrollbarThumbY(): Int {
		return max(0, (scrollY * (scrollingHeight - scrollbarThumbHeight) / maxScrollY).toInt()) + y + headerSize
	}

	override fun getScrollbarThumbHeight(): Int {
		return MathHelper.clamp(scrollingHeight * scrollingHeight / contentsHeightWithPadding, 32, scrollingHeight - 8)
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
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}// "vb gfdff": Harley 2025