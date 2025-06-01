package com.pineypiney.mtt.gui.widget.sheet_widgets

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.gui.screens.CharacterSheetScreen
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class ArmourClassWidget(val screen: CharacterSheetScreen, val character: Character, x: Int, y: Int, w: Int, h: Int, message: Text): ClickableWidget(x, y, w, h, message) {

	val label = Text.translatable("mtt.trait.armour_class.abbr")
	val labelWidth = screen.textRenderer.getWidth(label)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		context.drawTexture(RenderLayer::getGuiTextured, SHIELD, x, y, 0f, 0f, 40, 40, 64, 64)
		context.drawText(screen.textRenderer, label, x + (width - labelWidth) / 2, y + 25, 4210752, false)

		val armour = character.getTotalArmour()
		val armourText = Text.literal(armour.toString())
		val armourTextW = screen.textRenderer.getWidth(armourText) - 1

		context.matrices.push()
		context.matrices.scale(2f, 2f, 2f)
		val provider = context.vertexConsumers
		screen.textRenderer.draw(armourText, (x + width * .5f - armourTextW) * .5f, (y + 8) * .5f, 4210752, false, context.matrices.peek().positionMatrix, provider, TextRenderer.TextLayerType.NORMAL, 0, 15728880)
		context.matrices.pop()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

	}

	override fun getTooltip(): Tooltip? {
		return super.getTooltip()
	}

	companion object {
		val SHIELD = Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/ac_box.png")
	}
}