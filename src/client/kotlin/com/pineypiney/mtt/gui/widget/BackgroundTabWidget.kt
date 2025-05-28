package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.LiteralPart
import com.pineypiney.mtt.dnd.traits.TallyPart
import com.pineypiney.mtt.util.Localisation
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import kotlin.math.PI

class BackgroundTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, message: Text, backgrounds: Set<Background>) : CharacterCreatorOptionsTabWidget<Background>(sheet, client, x, y, width, height, message) {

	override val valueSelectChildren: List<Entry<Background>> = backgrounds.map {
		BackgroundEntry(it, this, 8, 32, 240, 20, Text.literal(it.id))
	}

	override fun renderWidget(
		context: DrawContext,
		mouseX: Int,
		mouseY: Int,
		deltaTicks: Float
	) {
		if(selected != null){
			val titleText = Text.translatable("mtt.background.${selected?.id}")
			val titleWidth = client.textRenderer.getWidth(titleText)
			val s = 2.5f
			val titleX = (x + (width - titleWidth * s) * .5f)
			val titleY = y - 20
			context.matrices.push()
			context.matrices.scale(s, s, s)
			context.drawText(client.textRenderer, titleText, (titleX/s).toInt(), (titleY/s).toInt(), 4210752, false)

			// New scale = 2.5 * 0.6 = 1.5
			context.matrices.scale(.6f, .6f, .6f)
			val backButtonX = ((x + 20) * 0.6666667f).toInt()
			val backButtonY = ((y - 21) * 0.6666667f).toInt()
			isBackButtonHovered = mouseX >= backButtonX * 1.5f && mouseY > backButtonY * 1.5f && mouseX < backButtonX * 1.5f + 18 && mouseY < backButtonY * 1.5f + 18
			if(isBackButtonHovered) DynamicWidgets.drawThinBox(context, backButtonX, backButtonY, 12, 12, -8947552, -3157769, -13158304)
			else DynamicWidgets.drawThinBox(context, backButtonX, backButtonY, 12, 12, -3750202, -1, -11184811)
			context.matrices.multiply(Quaternionf(AxisAngle4f(PI.toFloat() * .5f, 0f, 0f, 1f)), backButtonX + 6f, backButtonY + 6f, 0f)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/button_icon.png"), backButtonX + 2, backButtonY + 4, 0f, 0f, 8, 4, 16, 16)
			context.matrices.pop()
		}
		context.enableScissor(x, y, right, bottom)
		if(selected == null) {
			context.matrices.push()
			context.matrices.scale(2f, 2f, 2f)
			val entryX = x + 50
			for (i in 0..<valueSelectChildren.size) {
				val entryY = (y + 30 + i * 30)
				valueSelectChildren[i].render(context, client.textRenderer, entryX, entryY)
			}
			context.matrices.pop()
		}
		else {
			selectedPage.forEach { it.render(context, mouseX, mouseY, deltaTicks) }
		}
		context.disableScissor()

		optionSelectWidget?.let { widget ->
			context.fill(0, 0, client.currentScreen!!.width, client.currentScreen!!.height, 0, 2050307381)
			context.enableScissor(widget.x, widget.y, widget.x + widget.width, widget.y + widget.height)
			widget.render(context, mouseX, mouseY, deltaTicks)
			context.disableScissor()
		}
		drawScrollbar(context)
	}

	override fun setupSelectedPage(selected: Background) {
		val x = x + 20
		val w = width - 40
		selectedPage.add(TraitEntry.newOf(x, y + 0, w, this, Text.translatable("mtt.feature.proficiency"), 0, listOf(LiteralPart("mtt.feature.proficiency.declaration", Localisation.translateList(listOf(selected.skill1, selected.skill2), false){ prof -> "mtt.skill.${prof.id}" }))))
		selectedPage.add(TraitEntry.newOf(x, y + 15, w, this, Text.translatable(selected.tool.getLabelKey()), 1, selected.tool.getParts()))
		selectedPage.add(TraitEntry.newOf(x, y + 30, w, this, Text.translatable("mtt.feature.feat"), 2, listOf(LiteralPart("mtt.feature.feat.declaration", Text.translatable("mtt.feat.${selected.feat.id}")))))
		selectedPage.add(TraitEntry(x, y + 45, w, this, Text.translatable("mtt.feature.ability_score"), 3, listOf(
			TraitEntry.TallySegment(TallyPart(setOf(selected.ability1, selected.ability2, selected.ability3), 3){ "mtt.ability.${it.id}"}))
		))
	}

	class BackgroundEntry(background: Background, tab: BackgroundTabWidget, x: Int, y: Int, width: Int, height: Int, message: Text): Entry<Background>(background, tab, x, y, width, height, message){

		override val type: String = "background"
		override fun getID(value: Background): String = value.id

		override fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int) {
			setPosition(x, y)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/background_icons/${value.id}.png"), (x / 2), (y / 2), 0f, 0f, 8, 8, 8, 8)
			context.drawText(textRenderer, Text.translatable("mtt.background.${value.id}"), (x / 2) + 10, (y / 2) + 1, 4210752, false)
		}
	}
}