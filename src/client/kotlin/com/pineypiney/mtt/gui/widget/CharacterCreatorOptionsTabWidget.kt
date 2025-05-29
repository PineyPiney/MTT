package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.network.payloads.c2s.ClickButtonC2SPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.max

abstract class CharacterCreatorOptionsTabWidget<T>(sheet: CharacterSheet, client: MinecraftClient, x: Int, yOrigin: Int, width: Int, panelHeight: Int, text: Text) : CharacterCreatorTabWidget(sheet, client, x, yOrigin, width, panelHeight, text){

	override val headerHeight: Int get() = if(selected == null) 0 else 25
	override val isReady: Boolean get() = selected != null && selectedPage.all { it.isReady }
	abstract val valueSelectChildren: List<Entry<T>>

	var isBackButtonHovered = false
	var optionSelectWidget: OptionsSelectWidget<*>? = null

	val scrollingHeight get() = height - headerHeight - footerHeight

	protected var selected: T? = null
		set(value) {
			if(field != value) {
				field = value
				selectedPage.clear()
				if(value != null) {
					setupSelectedPage(value)
				}
				else {
					isBackButtonHovered = false
					selectedPage.clear()
				}
			}
		}
	val selectedPage: MutableList<TraitEntry> = mutableListOf()

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

	fun renderSelectedTitle(context: DrawContext, title: String, mouseX: Int, mouseY: Int){
		if(selected != null){
			val titleText = Text.translatable(title)
			val titleWidth = client.textRenderer.getWidth(titleText)
			val s = 2.5f
			val titleX = (x + (width - titleWidth * s) * .5f)
			val titleY = y + 5
			context.matrices.push()
			context.matrices.scale(s, s, s)
			context.drawText(client.textRenderer, titleText, (titleX/s).toInt(), (titleY/s).toInt(), 4210752, false)

			// New scale = 2.5 * 0.6 = 1.5
			context.matrices.scale(.6f, .6f, .6f)
			val backButtonX = ((x + 20) * 0.6666667f).toInt()
			val backButtonY = ((y + 4) * 0.6666667f).toInt()
			isBackButtonHovered = mouseX >= backButtonX * 1.5f && mouseY > backButtonY * 1.5f && mouseX < backButtonX * 1.5f + 18 && mouseY < backButtonY * 1.5f + 18
			if(isBackButtonHovered) DynamicWidgets.drawThinBox(context, backButtonX, backButtonY, 12, 12, -8947552, -3157769, -13158304)
			else DynamicWidgets.drawThinBox(context, backButtonX, backButtonY, 12, 12, -3750202, -1, -11184811)
			context.matrices.multiply(Quaternionf(AxisAngle4f(PI.toFloat() * .5f, 0f, 0f, 1f)), backButtonX + 6f, backButtonY + 6f, 0f)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/button_icon.png"), backButtonX + 2, backButtonY + 4, 0f, 0f, 8, 4, 16, 16)
			context.matrices.pop()
		}
	}

	fun renderContent(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float){
		context.enableScissor(x, y + headerHeight, right, bottom - footerHeight)
		if(selected == null) {
			val s = 3f
			context.matrices.push()
			context.matrices.scale(s, s, s)
			val entryX = x + 50
			for (i in 0..<valueSelectChildren.size) {
				val entryY = (y + 30 + i * 30)
				valueSelectChildren[i].render(context, client.textRenderer, entryX, entryY, s)
			}
			context.matrices.pop()
		}
		else {
			selectedPage.forEach { it.render(context, mouseX, mouseY, deltaTicks) }
		}

		context.disableScissor()
	}

	fun renderOptionWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float){
		optionSelectWidget?.let { widget ->
			context.fill(0, 0, client.currentScreen!!.width, client.currentScreen!!.height, 0, 2050307381)
			context.enableScissor(widget.x, widget.y, widget.x + widget.width, widget.y + widget.height)
			widget.render(context, mouseX, mouseY, deltaTicks)
			context.disableScissor()
		}
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
			DynamicWidgets.drawThinBox(context, x + width - 14, y + headerHeight, 14, scrollingHeight, -7631989, -13158601, -1)
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
			-1 -> (y + 25 - scrollY.toInt()) to 0..<selectedPage.size
			else -> selectedPage[start].y + selectedPage[start].height + 2 to (start + 1)..<selectedPage.size
		}
		for (i in range) {
			val entry = selectedPage[i]
			entry.x = x + 20
			entry.y = y
			y += entry.height + 2
		}
	}

	override fun getMaxScrollY(): Int {
		return max(0, contentsHeightWithPadding - scrollingHeight)
	}

	override fun getScrollbarThumbY(): Int {
		return max(0, (scrollY * (scrollingHeight - scrollbarThumbHeight) / maxScrollY).toInt()) + y + headerHeight
	}

	override fun getScrollbarThumbHeight(): Int {
		return MathHelper.clamp(scrollingHeight * scrollingHeight / contentsHeightWithPadding, 32, scrollingHeight - 8)
	}

	abstract class Entry<T>(val value: T, val tab: CharacterCreatorOptionsTabWidget<T>, x: Int, y: Int, width: Int, height: Int, message: Text): ClickableWidget(x, y, width, height, message){

		abstract val type: String

		override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {

		}

		abstract fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int, scale: Float)

		abstract fun getID(value: T): String
		override fun onClick(mouseX: Double, mouseY: Double) {
			val payload = ClickButtonC2SPayload(type, getID(value))
			ClientPlayNetworking.send(payload)
			tab.selected = value
		}

		override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

		}
	}
}