package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.CharacterSheet
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CreatureType
import com.pineypiney.mtt.dnd.Size
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.SubspeciesIDComponent
import com.pineypiney.mtt.dnd.traits.Trait
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import kotlin.math.PI

class SpeciesTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, yOrigin: Int, width: Int, panelHeight: Int, message: Text, private val species: List<Species>) : CharacterCreatorOptionsTabWidget<Species>(
	sheet, client,
	x,
	yOrigin,
	width,
	panelHeight,
	message
) {

	override val valueSelectChildren = species.mapIndexed { i, species -> SpeciesEntry(species, 0, 0, width - 100, 24, 3f, Text.literal("Species Entry")){ species ->
		selected = species
	} }

	override fun renderWidget(
		context: DrawContext,
		mouseX: Int,
		mouseY: Int,
		deltaTicks: Float
	) {
		if(selected != null){
			val titleText = Text.translatable("mtt.species.${selected?.id}")
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
			val s = 3f
			context.matrices.push()
			context.matrices.scale(s, s, s)
			val entryX = x + 50
			for (i in 0..<species.size) {
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

	override fun setupSelectedPage(selected: Species){
		selectedPage.add(TraitEntry<CreatureType>(x + 20, y, width - 40, this, Text.translatable("mtt.trait.creature_type"), 0, listOf(
			TraitEntry.FormattedTrait("mtt.trait.creature_type.declaration", SetTraits(selected.type)), Text.translatable("mtt.trait.creature_type.description")), {"mtt.creature_type.${it.name.lowercase()}"}){ values ->
			sheet.type = values.firstOrNull() ?: return@TraitEntry
		})
		selectedPage.add(TraitEntry<Size>(x + 20, y + 15, width - 40, this, Text.translatable("mtt.trait.size"), 1, listOf(TraitEntry.FormattedTrait("mtt.trait.size.declaration", selected.size), Text.translatable("mtt.trait.size.description")), {"mtt.size.${it.name}"}){ values ->
			sheet.size = values.firstOrNull() ?: return@TraitEntry
		})
		selectedPage.add(TraitEntry(x + 20, y + 30, width - 40, this, Text.translatable("mtt.trait.speed"), 2, listOf(TraitEntry.FormattedTrait("mtt.trait.speed.declaration", SetTraits(selected.speed)), Text.translatable("mtt.trait.speed.description")), {"$it ft"}){ values ->
			sheet.speed = values.firstOrNull() ?: return@TraitEntry
		})
		selectedPage.add(TraitEntry<String>(x + 20, y + 45, width - 40, this, Text.translatable("mtt.trait.model"), 3, listOf(TraitEntry.FormattedTrait("mtt.trait.model.declaration", selected.model), Text.translatable("mtt.trait.model.description")), {"mtt.model.$it"}) { values ->
			sheet.model = values.firstOrNull() ?: return@TraitEntry
		})
		selected.components.forEachIndexed { i, comp ->
			if(comp is SubspeciesIDComponent) return@forEachIndexed
			val lines = comp.getLines().toMutableList()
			for(i in 0..<lines.size){
				val value = lines[i]
				if(value is Trait<*>) lines[i] = TraitEntry.FormattedTrait(comp.declarationKey, value)
			}
			selectedPage.add(TraitEntry(x + 20, y + 60 + 15 * i, width - 40, this, comp.getLabel(), i + 4, lines, comp::getTranslationKey){

			})
		}
	}

	class SpeciesEntry(species: Species, x: Int, y: Int, width: Int, height: Int, private val renderScale: Float, message: Text, onClick: (Species) -> Unit): Entry<Species>(species, x, y, width, height, message, onClick){

		override fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int){
			setPosition(x, y)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/species_icons/${value.id}.png"), (x / renderScale).toInt(), (y / renderScale).toInt(), 0f, 0f, 8, 8, 8, 8)
			context.drawText(textRenderer, Text.translatable("mtt.species.${value.id}"), (x / renderScale).toInt() + 10, (y / renderScale).toInt() + 1, 4210752, false)
		}
	}
}