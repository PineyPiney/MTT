package com.pineypiney.mtt.gui.widget.ability_widget

import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.AbilityPointBuyPart
import com.pineypiney.mtt.gui.widget.AbilitiesTabWidget
import com.pineypiney.mtt.gui.widget.DynamicWidgets
import com.pineypiney.mtt.network.payloads.c2s.UpdateTraitC2SPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

class PointBuyWidget(tab: AbilitiesTabWidget, x: Int, y: Int, w: Int, h: Int): AbilitySelectorWidget(tab, x, y, w, h, Text.literal("Point Buy Widget")) {

	val part = AbilityPointBuyPart()
	override val isReady: Boolean get() = part.isReady()
	override val points: IntArray get() = part.points

	val labelText = Text.translatable("mtt.character_maker_screen.points_left")
	val labelW = tab.client.textRenderer.getWidth(labelText)

	var hoveredButton = -1

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {

		context.drawText(tab.client.textRenderer, labelText, x + (width - labelW) / 2, y, 4210752, false)

		val pointTallyText = Text.literal("${part.pointsLeft}/${part.totalPoints}")
		val tallyW = tab.client.textRenderer.getWidth(pointTallyText)

		context.drawText(tab.client.textRenderer, pointTallyText, x + (width - tallyW) / 2, y + 12, 4210752, false)

		hoveredButton = -1
		val hoveringMinusX = mouseX >= right - 50 && mouseX < right - 37
		val hoveringPlusX = mouseX >= right - 13 && mouseX < right
		for(i in 0..5){
			val y = y + 32 + 16 * i
			val ability = Ability.entries[i]

			val hoveringAbilityY = mouseY >= y && mouseY < y + 13
			if(hoveringMinusX && hoveringAbilityY) hoveredButton = i * 2
			else if(hoveringPlusX && hoveringAbilityY) hoveredButton = i * 2 + 1

			val baseColour = -3750202

			val abilityText = Text.translatable("mtt.ability.${ability.id}")
			context.drawText(tab.client.textRenderer, abilityText, x, y + 2, 4210752, false)

			DynamicWidgets.drawThinBox(context, right - 50, y, 13, 13, baseColour, -1, -9605779)
			if(hoveredButton == i * 2) context.drawBorder(right - 50, y, 13, 13, -1)
			context.drawHorizontalLine(right - 46, right - 42, y + 6, -12632257)

			DynamicWidgets.drawThinBox(context, right - 13, y, 13, 13, baseColour, -1, -9605779)
			if(hoveredButton == i * 2 + 1) context.drawBorder(right - 13, y, 13, 13, -1)
			context.drawHorizontalLine(right - 9, right - 5, y + 6, -12632257)
			context.drawVerticalLine(right - 7, y + 3, y + 9, -12632257)

			val scoreText = Text.literal(points[i].toString())
			val scoreW = tab.client.textRenderer.getWidth(scoreText)
			context.drawText(tab.client.textRenderer, scoreText, right - 25 - scoreW / 2, y + 2, 4210752, false)
		}
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if(hoveredButton != -1){
			val ability = hoveredButton shr 1
			val plus = (hoveredButton and 1) == 1
			val func = if(plus) AbilityPointBuyPart::increment else AbilityPointBuyPart::decrement
			if(func(part, ability)) ClientPlayNetworking.send(UpdateTraitC2SPayload("ability", 0, 0, listOf(ability.toString(), points[ability].toString())))
			return true
		}
		else return false
	}
}