package com.pineypiney.mtt.client.gui.widget.sheet_widgets

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.client.gui.widget.DynamicWidgets
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Ability
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class AbilityWidget(
	val screen: CharacterSheetScreen,
	val ability: Ability,
	val character: Character,
	x: Int,
	y: Int,
	w: Int,
	h: Int,
	message: Text,
	val textColour: Int = -12566464
) : ClickableWidget(x, y, w, h, message) {

	val label = Text.translatable("mtt.ability.${ability.id}.abbr")
	val labelWidth = screen.textRenderer.getWidth(label)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		DynamicWidgets.drawRoundedBorder(context, x, y, 28, 35)
		DynamicWidgets.drawRoundedBorder(context, x + 5, y + 27, 18, 11, -12632257, -3750202)
		//context.drawTexture(RenderLayer::getGuiTextured, BOX, x, y, 0f, 0f, 28, 38, 32, 64)
		context.drawText(screen.textRenderer, label, x + (width - labelWidth) / 2, y + 2, textColour, false)

		val score = character.abilities[ability]
		val mod = character.abilities.getMod(ability)
		val modText = Text.literal(if(mod > 0) "+$mod" else mod.toString())
		val modTextW = screen.textRenderer.getWidth(modText) - 1

		context.matrices.pushMatrix()
		context.matrices.scale(2f, 2f)

		context.matrices.translate((x + width * .5f - modTextW) * .5f, (y + 11) * .5f)
		context.drawText(screen.textRenderer, modText, 0, 0, textColour, false)

		context.matrices.popMatrix()

		val scoreText = Text.literal(score.toString())
		val scoreTextW = screen.textRenderer.getWidth(scoreText)
		context.drawText(screen.textRenderer, scoreText, (x + (width - scoreTextW) / 2), y + 29, textColour, false)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

	}

	companion object {
		val BOX = Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/ability_box.png")
	}
}