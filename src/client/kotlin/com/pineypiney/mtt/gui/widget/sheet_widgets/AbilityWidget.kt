package com.pineypiney.mtt.gui.widget.sheet_widgets

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.gui.widget.DynamicWidgets
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AbilityWidget(val screen: CharacterSheetScreen, val ability: Ability, val character: Character, x: Int, y: Int, w: Int, h: Int, message: Text): ClickableWidget(x, y, w, h, message) {

	val label = Text.translatable("mtt.ability.${ability.id}.abbr")
	val labelWidth = screen.textRenderer.getWidth(label)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		DynamicWidgets.drawRoundedBorder(context, x, y, 28, 35)
		DynamicWidgets.drawRoundedBorder(context, x + 5, y + 27, 18, 11, -12632257, -3750202)
		//context.drawTexture(RenderLayer::getGuiTextured, BOX, x, y, 0f, 0f, 28, 38, 32, 64)
		context.drawText(screen.textRenderer, label, x + (width - labelWidth) / 2, y + 2, 4210752, false)

		val score = character.abilities[ability]
		val mod = character.abilities.getMod(ability)
		val modText = Text.literal(if(mod > 0) "+$mod" else mod.toString())
		val modTextW = screen.textRenderer.getWidth(modText) - 1

		context.matrices.push()
		context.matrices.scale(2f, 2f, 2f)
		context.vertexConsumers
		val provider = context.vertexConsumers
		screen.textRenderer.draw(modText, (x + width * .5f - modTextW) * .5f, (y + 11) * .5f, 4210752, false, context.matrices.peek().positionMatrix, provider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
		//context.drawText(screen.textRenderer, modText, (x + width / 2 - modTextW) / 2, (y + 11) / 2, 4210752, false)
		context.matrices.pop()

		val scoreText = Text.literal(score.toString())
		val scoreTextW = screen.textRenderer.getWidth(scoreText)
		context.drawText(screen.textRenderer, scoreText, (x + (width - scoreTextW) / 2), y + 29, 4210752, false)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

	}

	override fun getTooltip(): Tooltip? {
		return super.getTooltip()
	}

	companion object {
		val BOX = Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/ability_box.png")
	}
}