package com.pineypiney.mtt.client.gui.widget.sheet_widgets

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.gui.screens.CharacterSheetScreen
import com.pineypiney.mtt.dnd.characters.Character
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class ArmourClassWidget(
	val screen: CharacterSheetScreen,
	val character: Character,
	x: Int,
	y: Int,
	w: Int,
	h: Int,
	message: Text,
	val textColour: Int = -12566464
) : ClickableWidget(x, y, w, h, message) {

	val label = Text.translatable("mtt.armour_class.abbr")
	val labelWidth = screen.textRenderer.getWidth(label)

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		context.drawTexture(RenderPipelines.GUI_TEXTURED, SHIELD, x, y, 0f, 0f, 40, 40, 64, 64)
		context.drawText(screen.textRenderer, label, x + (width - labelWidth) / 2, y + 25, textColour, false)

		val armour = character.getTotalArmour()
		val armourText = Text.literal(armour.toString())
		val armourTextW = screen.textRenderer.getWidth(armourText) - 1

		context.matrices.pushMatrix()
		context.matrices.scale(2f, 2f)
		context.matrices.translate((x + width * .5f - armourTextW) * .5f, (y + 8) * .5f)
		context.drawText(screen.textRenderer, armourText, 0, 0, textColour, false)

		context.matrices.popMatrix()
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder) {

	}

	companion object {
		val SHIELD = Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/ac_box.png")
	}
}