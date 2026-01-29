package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.traits.LiteralPart
import com.pineypiney.mtt.dnd.traits.Source
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class RaceTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, yOrigin: Int, width: Int, panelHeight: Int, message: Text, races: Set<Race>) : CharacterCreatorOptionsTabWidget<Race>(
	sheet, client,
	x,
	yOrigin,
	width,
	panelHeight,
	message
) {

	override val valueSelectChildren = races.map {
		RaceEntry(it, this, 0, 0, width - 100, 24, Text.literal("Race Entry"))
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		renderSelectedTitle(context, "mtt.race.${selected?.id}", mouseX, mouseY)
		renderContent(context, mouseX, mouseY, deltaTicks)
		renderOptionWidget(context, mouseX, mouseY, deltaTicks)
		drawScrollbar(context)
	}

	override fun setupSelectedPage(selected: Race){
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
			selectedPage.add(TraitEntry.newOf(x + 20, y + 15 * i, width - 40, this, trait.getLabel(), i++, trait.getParts()))
		}
		selected.namedTraits.forEach { namedTrait ->
			selectedPage.add(TraitEntry.newOf(x + 20, y + 15 * i, width - 40, this, Text.translatable("mtt.feature.${namedTrait.name}"), i++, namedTrait.traits.flatMap { it.getParts() }.toSet()))
		}
	}

	override fun apply(sheet: CharacterSheet) {
		val src = Source.RaceSource(selected ?: return)
		for(trait in selectedPage){
			trait.updateValues(sheet, src)
		}
	}

	class RaceEntry(race: Race, tab: RaceTabWidget, x: Int, y: Int, width: Int, height: Int, message: Text): Entry<Race>(race, tab, x, y, width, height, message){

		override val type: String = "race"
		override fun getID(value: Race): String = value.id

		override fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int, scale: Float){
			setPosition(x, y)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/race_icons/${value.id}.png"), (x / scale).toInt(), (y / scale).toInt(), 0f, 0f, 8, 8, 8, 8)
			context.drawText(textRenderer, Text.translatable("mtt.race.${value.id}"), (x / scale).toInt() + 10, (y / scale).toInt() + 1, 4210752, false)
		}
	}
}