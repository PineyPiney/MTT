package com.pineypiney.mtt.client.gui.widget

import com.pineypiney.mtt.MTT
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ScrollableWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class DropDownListWidget<T>(x: Int, y: Int, width: Int, height: Int, val entries: List<T>, val textRenderer: TextRenderer, val getText: (T) -> Text, val entryHeight: Int, message: Text) : ScrollableWidget(x, y, width, height, message) {

	var open = false
		set(value) {
			field = value
			height = if(value) getDropdownHeight() else entryHeight
		}
	var selected: T? = null

	override fun getContentsHeightWithPadding(): Int {
		return entries.size * entryHeight
	}

	override fun getDeltaYPerScroll(): Double {
		return entryHeight * 0.5
	}

	override fun onClick(click: Click, double: Boolean) {
		if (openButtonHovered(click.x.toInt(), click.y.toInt())) {
			open = !open
		}
		else if(open){
			val hovered = getHoveredEntry(click.x.toInt(), click.y.toInt())
			if(hovered != -1){
				selected = entries.getOrNull(hovered)
				open = false
			}
		}
	}

	override fun renderWidget(
		context: DrawContext,
		mouseX: Int,
		mouseY: Int,
		deltaTicks: Float
	) {
		if(!open) {
			context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, 50, 12, 64, 16)
			selected?.let {
				context.drawText(textRenderer, getText(it), x + 2, y + 3, -1, false)
			}

			context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 51, y, 52f, 0f, 12, 12, 64, 16)
			if(openButtonHovered(mouseX, mouseY)){
				context.drawStrokedRectangle(x + 51, y, 12, 12, -1)
			}
		}
		else {
			val height = getDropdownHeight()
			context.fill(x, y, x + 49, y+1, -11184811)
			context.fill(x, y + 1, x + 1, y + height - 1, -11184811)
			context.fill(x + 49, y, x + 50, y+1, -7631989)
			context.fill(x, y + height - 1, x + 1, y + height, -7631989)
			context.fill(x + 1, y + height - 1, x + 49, y + height, -1)
			context.fill(x + 49, y + 1, x + 50, y + height, -1)
			context.fill(x + 1, y + 1, x + 49, y + height - 1, -7631989)
			for((i, entry) in entries.withIndex()){
				val y = y + i * entryHeight + 3
				context.drawText(textRenderer, getText(entry), x + 2, y, -1, false)
			}
			val hovered = getHoveredEntry(mouseX, mouseY)
			if(hovered != -1) {
				val hoveredY =  y + 1 + (hovered * entryHeight)
				context.fill(x + 1, hoveredY, x + 49, hoveredY + entryHeight, 2144128204)
			}
			context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 51, y, 52f, 0f, 12, 12, 64, 16)

		}
	}

	fun getDropdownHeight(): Int = entries.size * entryHeight + 2

	fun openButtonHovered(mouseX: Int, mouseY: Int): Boolean {
		return mouseX >= x + 51 && mouseY >= y && mouseX < x + 63 && mouseY < y + 12
	}

	fun getHoveredEntry(mouseX: Int, mouseY: Int): Int{
		if(mouseX <= x || mouseY <= y || mouseX >= x + 49 || mouseY >= y + getDropdownHeight() - 1) return -1
		return (mouseY - y - 1).floorDiv(entryHeight)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}

	companion object {
		val TEXTURE: Identifier = Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/dropdown.png")
	}
}