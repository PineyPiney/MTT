package com.pineypiney.mtt.gui.widget.sheet_widgets

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.mixin_interfaces.VertexConsumerProviderGetter
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AbilityWidget(val screen: CharacterSheetScreen, val ability: Ability, val sheet: CharacterSheet, x: Int, y: Int, w: Int, h: Int, message: Text): ClickableWidget(x, y, w, h, message) {

	val label = Text.translatable("mtt.ability.${ability.id}.abbr")
	val labelWidth = screen.textRenderer.getWidth(label)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		context.drawTexture(RenderLayer::getGuiTextured, BOX, x, y, 0f, 0f, 28, 38, 32, 64)
		context.drawText(screen.textRenderer, label, x + (width - labelWidth) / 2, y + 2, 4210752, false)

		val score = sheet.abilities[ability]
		val mod = sheet.abilities.getMod(ability)
		val modText = Text.literal(if(mod > 0) "+$mod" else mod.toString())
		val modTextW = screen.textRenderer.getWidth(modText) - 1

		context.matrices.push()
		context.matrices.scale(2f, 2f, 2f)
		val provider = (context as VertexConsumerProviderGetter).provider
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