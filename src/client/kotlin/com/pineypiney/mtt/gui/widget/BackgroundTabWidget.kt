package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.TraitCodec
import com.pineypiney.mtt.dnd.traits.feats.Feat
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import kotlin.math.PI

class BackgroundTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, message: Text, backgrounds: Set<Background>) : CharacterCreatorOptionsTabWidget<Background>(
	sheet, client,
	x,
	y,
	width,
	height,
	message
) {

	override val valueSelectChildren: List<Entry<Background>> = backgrounds.map {
		BackgroundEntry(it, 8, 32, 240, 20, Text.literal(it.id)){ background ->
			selected = background
		}
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
		selectedPage.add(TraitEntry.of<Proficiency>(x, y + 0, w, this, Text.translatable("mtt.feature.proficiency"), 0, listOf(TraitEntry.FormattedTrait("mtt.feature.proficiency.declaration", SetTraits(CharacterSheet::addProficiencies, selected.skill1, selected.skill2)))){ prof -> "mtt.skill.${prof.id}" })
		selectedPage.add(TraitEntry.of<Proficiency>(x, y + 15, w, this, Text.translatable("mtt.feature.proficiency"), 1, listOf(TraitEntry.FormattedTrait("mtt.feature.proficiency.declaration", selected.tool))){ prof -> "mtt.tool.${prof.id}" })
		selectedPage.add(TraitEntry.of<Feat>(x, y + 30, w, this, Text.translatable("mtt.feature.feat"), 2, listOf(TraitEntry.FormattedTrait("mtt.feature.feat.declaration", SetTraits(TraitCodec.FEAT_CODEC::apply, selected.feat)))){ feat -> "mtt.feat.${feat.id}" })
		selectedPage.add(TraitEntry(x, y + 45, w, this, Text.translatable("mtt.feature.ability_score"), 3, listOf(AbilityScoreSegment(setOf(selected.ability1, selected.ability2, selected.ability3), 3, client.textRenderer))))
	}

	class AbilityScoreSegment(abilities: Set<Ability>, val points: Int, textRenderer: TextRenderer) : TraitEntry.Segment<Ability> {

		val labels = abilities.map { Text.translatable("mtt.ability.${it.id}") }
		private val map = abilities.map { 0 }.toMutableList()
		override val height: Int get() = 9 + 12 * map.size
		val pointsLeft get() = points - map.sum()

		var hoveredIcon = -1

		override fun render(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int, i: Int, entry: TraitEntry<Ability>, shadow: Boolean) {
			val textRenderer = entry.tab.client.textRenderer
			val labelWidth = labels.maxOf { textRenderer.getWidth(it) } + 20

			val pointsText = Text.literal("$pointsLeft/$points")
			ctx.drawText(textRenderer, pointsText, x + labelWidth + 5 * points + 3 - textRenderer.getWidth(pointsText) / 2, y, 16777215, shadow)

			var i = 0
			hoveredIcon = -1
			for(value in map){
				val buttonY = y + 12 * (i + 1)
				val hoveringRow = mouseY >= buttonY && mouseY < buttonY + 7
				ctx.drawText(textRenderer, labels[i], x + labelWidth - textRenderer.getWidth(labels[i]), buttonY, 16777215, shadow)
				for(j in 0..<points){
					val on = value > j
					val buttonX = x + labelWidth + 5 + 10 * j
					val hoveringButton = hoveringRow && mouseX >= buttonX && mouseX < buttonX + 7
					if(hoveringButton) hoveredIcon = (i shl 8) or (j and 255)
					val backgroundColour = if(on) -1385984 else -16777216

					ctx.drawHorizontalLine(buttonX + 1, buttonX + 5, buttonY, backgroundColour)
					ctx.drawHorizontalLine(buttonX + 1, buttonX + 5, buttonY + 6, backgroundColour)
					ctx.drawVerticalLine(buttonX, buttonY, buttonY + 6, backgroundColour)
					ctx.drawVerticalLine(buttonX + 6, buttonY, buttonY + 6, backgroundColour)

					if(on){
						if(hoveringButton) ctx.fill(buttonX + 1, buttonY + 1, buttonX + 6, buttonY + 6, 1575672320)
						ctx.fill(buttonX + 2, buttonY + 2, buttonX + 5, buttonY + 5, -1385984)
					}
					else{
						if(hoveringButton) ctx.fill(buttonX + 2, buttonY + 2, buttonX + 5, buttonY + 5, 1575672320)
					}
				}
				i++
			}
		}

		override fun onClick(mouseX: Double, mouseY: Double, entry: TraitEntry<Ability>): Boolean {
			if(hoveredIcon != -1){
				val row = hoveredIcon shr 8
				val index = hoveredIcon and 255
				val score = map[row]
				val adding = index >= score
				if(adding && pointsLeft > 0) map[row]++
				else if(!adding && map[row] > 0) map[row]--
				return true
			}
			return false
		}
	}

	class BackgroundEntry(background: Background, x: Int, y: Int, width: Int, height: Int, message: Text, onClick: (Background) -> Unit): Entry<Background>(background, x, y, width, height, message, onClick){
		override fun render(
			context: DrawContext,
			textRenderer: TextRenderer,
			x: Int,
			y: Int
		) {
			setPosition(x, y)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/background_icons/${value.id}.png"), (x / 2), (y / 2), 0f, 0f, 8, 8, 8, 8)
			context.drawText(textRenderer, Text.translatable("mtt.background.${value.id}"), (x / 2) + 10, (y / 2) + 1, 4210752, false)
		}

	}
}