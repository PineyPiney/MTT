package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.dnd.CharacterSheet
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text

abstract class CharacterCreatorOptionsTabWidget<T>(sheet: CharacterSheet, client: MinecraftClient, x: Int, yOrigin: Int, width: Int, panelHeight: Int, text: Text) : CharacterCreatorTabWidget(sheet, client, x, yOrigin, width, panelHeight, text){

	abstract val valueSelectChildren: List<Entry<T>>

	var isBackButtonHovered = false
	var optionSelectWidget: OptionsSelectWidget<*>? = null

	protected var selected: T? = null
		set(value) {
			if(field != value) {
				field = value
				selectedPage.clear()
				if(value != null) {
					yOffset = 25
					y = yOrigin + yOffset
					height = panelHeight - yOffset
					setupSelectedPage(value)
				}
				else {
					yOffset = 0
					y = yOrigin
					height = panelHeight
					isBackButtonHovered = false
					selectedPage.clear()
				}
			}
		}
	val selectedPage: MutableList<TraitEntry<*>> = mutableListOf()

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		// If there is a pop up options widget then nothing else should be interactable
		if(optionSelectWidget != null) {
			return optionSelectWidget?.mouseClicked(mouseX, mouseY, button) == true
		}
		if(isBackButtonHovered) {
			optionSelectWidget = null
			selected = null
			selectedPage.clear()
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
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
		if(optionSelectWidget == null) {
			super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
			reposition()
		}
		else optionSelectWidget?.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
		return true
	}

	override fun getContentsHeightWithPadding(): Int {
		return if(selected == null) valueSelectChildren.size * 30
		else selectedPage.sumOf { it.height + 2 }
	}

	override fun children(): List<Element> {
		return if(selected == null) valueSelectChildren else selectedPage
	}

	override fun drawScrollbar(context: DrawContext) {
		if(overflows()){
			DynamicWidgets.drawThinBox(context, x + width - 14, y, 14, height, -7631989, -13158601, -1)
			DynamicWidgets.drawScroller(context, x + width - 13, scrollbarThumbY + 1, 12, scrollbarThumbHeight - 2)
		}
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		return active && visible && mouseX >= x && mouseY >= yOrigin && mouseX < right && mouseY < yOrigin + panelHeight
	}

	abstract fun setupSelectedPage(selected: T)

	override fun reposition(start: Int) {
		optionSelectWidget?.let {
			val height = client.currentScreen?.height ?: return
			// Place the options picker in the middle of the screen
			it.x = x + (width - it.width) / 2
			it.reposition(height / 2)
		}

		var (y, range) = when (start) {
			-1 -> (y - scrollY.toInt()) to 0..<selectedPage.size
			else -> selectedPage[start].y + selectedPage[start].height + 2 to (start + 1)..<selectedPage.size
		}
		for (i in range) {
			val entry = selectedPage[i]
			entry.x = x + 20
			entry.y = y
			y += entry.height + 2
		}
	}

	abstract class Entry<T>(val value: T, x: Int, y: Int, width: Int, height: Int, message: Text, val onClick: (T) -> Unit): ClickableWidget(x, y, width, height, message){
		override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {

		}

		abstract fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int)

		override fun onClick(mouseX: Double, mouseY: Double) {
			onClick(value)
		}

		override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

		}
	}
}