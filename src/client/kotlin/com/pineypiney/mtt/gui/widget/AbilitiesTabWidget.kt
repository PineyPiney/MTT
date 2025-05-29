package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.gui.widget.ability_widget.AbilitySelectorWidget
import com.pineypiney.mtt.gui.widget.ability_widget.PointBuyWidget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.text.Text

class AbilitiesTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, message: Text) : CharacterCreatorTabWidget(
	sheet, client,
	x,
	y,
	width,
	height,
	message
) {

	override val isReady: Boolean get() = abilitySelectWidget.isReady
	var abilitySelectWidget: AbilitySelectorWidget = PointBuyWidget(this, x + 50, y + 32, width - 100, height - 40)

	override fun getContentsHeightWithPadding(): Int {
		return abilitySelectWidget.height
	}

	override fun getDeltaYPerScroll(): Double {
		return 15.0
	}

	override fun children(): List<Element?>? {
		return listOf(abilitySelectWidget)
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		context.enableScissor(x, y, right, bottom)
		val titleText = Text.translatable("mtt.character_maker_screen.abilities")
		val titleWidth = client.textRenderer.getWidth(titleText)
		val s = 2.5f
		val titleX = (x + (width - titleWidth * s) * .5f)
		val titleY = y + 5
		context.matrices.push()
		context.matrices.scale(s, s, s)
		context.drawText(client.textRenderer, titleText, (titleX/s).toInt(), (titleY/s).toInt(), 4210752, false)
		context.matrices.pop()

		abilitySelectWidget.render(context, mouseX, mouseY, deltaTicks)

		context.disableScissor()
	}

	override fun apply(sheet: CharacterSheet) {
		abilitySelectWidget.apply(sheet)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if(abilitySelectWidget.mouseClicked(mouseX, mouseY, button)) return true
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
		if(abilitySelectWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
	}

	override fun reposition(start: Int) {
		abilitySelectWidget.setPosition(x + 50, y + 32)
		abilitySelectWidget.setDimensions(width - 100, height - 40)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}