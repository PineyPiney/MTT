package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.traits.LiteralPart
import com.pineypiney.mtt.dnd.traits.Source
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SpeciesTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, yOrigin: Int, width: Int, panelHeight: Int, message: Text, species: Set<Species>) : CharacterCreatorOptionsTabWidget<Species>(
	sheet, client,
	x,
	yOrigin,
	width,
	panelHeight,
	message
) {

	override val valueSelectChildren = species.map {
		SpeciesEntry(it, this, 0, 0, width - 100, 24, Text.literal("Species Entry"))
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		renderSelectedTitle(context, "mtt.species.${selected?.id}", mouseX, mouseY)
		renderContent(context, mouseX, mouseY, deltaTicks)
		renderOptionWidget(context, mouseX, mouseY, deltaTicks)
		drawScrollbar(context)
	}

	override fun setupSelectedPage(selected: Species){
		val y = y + 25
		selectedPage.add(TraitEntry.newOf(x + 20, y, width - 40, this, Text.translatable("mtt.trait.creature_type"), 0, listOf(
			LiteralPart("mtt.trait.creature_type.declaration", Text.translatable("mtt.creature_type.${selected.type.name.lowercase()}"))
		)))
		selectedPage.add(TraitEntry.newOf(x + 20, y + 15, width - 40, this, Text.translatable("mtt.trait.size"), 1, selected.size.getParts()))
		selectedPage.add(TraitEntry.newOf(x + 20, y + 30, width - 40, this, Text.translatable("mtt.trait.speed"), 2, listOf(
			LiteralPart("mtt.trait.speed.declaration", "${selected.speed} ft"),
		)))
		selectedPage.add(TraitEntry.newOf(x + 20, y + 45, width - 40, this, Text.translatable("mtt.trait.model"), 3, selected.model.getParts()))
		var i = 4
		selected.traits.forEach { trait ->
			selectedPage.add(TraitEntry.newOf(x + 20, y + 15 * i, width - 40, this, Text.translatable(trait.getLabelKey()), i++, trait.getParts()))
		}
		selected.namedTraits.forEach { namedTrait ->
			selectedPage.add(TraitEntry.newOf(x + 20, y + 15 * i, width - 40, this, Text.translatable("mtt.feature.${namedTrait.name}"), i++, namedTrait.traits.flatMap { it.getParts() }.toSet()))
		}
	}

	override fun apply(sheet: CharacterSheet) {
		val src = Source.SpeciesSource(selected ?: return)
		for(trait in selectedPage){
			trait.updateValues(sheet, src)
		}
	}

	class SpeciesEntry(species: Species, tab: SpeciesTabWidget, x: Int, y: Int, width: Int, height: Int, message: Text): Entry<Species>(species, tab, x, y, width, height, message){

		override val type: String = "species"
		override fun getID(value: Species): String = value.id

		override fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int, scale: Float){
			setPosition(x, y)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/species_icons/${value.id}.png"), (x / scale).toInt(), (y / scale).toInt(), 0f, 0f, 8, 8, 8, 8)
			context.drawText(textRenderer, Text.translatable("mtt.species.${value.id}"), (x / scale).toInt() + 10, (y / scale).toInt() + 1, 4210752, false)
		}
	}
}