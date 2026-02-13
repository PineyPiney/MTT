package com.pineypiney.mtt.client.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.gui.screens.MTTScreens
import com.pineypiney.mtt.dnd.characters.CharacterModel
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.traits.LiteralPart
import com.pineypiney.mtt.dnd.traits.OneChoicePart
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.network.payloads.c2s.ClickButtonC2SPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class RaceTabWidget(
	sheet: CharacterSheet,
	client: MinecraftClient,
	x: Int,
	y: Int,
	width: Int,
	height: Int,
	message: Text,
	races: Set<Race>
) : CharacterCreatorOptionsTabWidget<Race>(
	sheet, client,
	x,
	y,
	width,
	height,
	message
) {

	override val valueSelectChildren = races.map {
		RaceEntry(it, this, 0, 0, width - 100, 24, Text.literal("Race Entry"))
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		renderSelectedTitle(context, "mtt.race.${selected?.id}", mouseX, mouseY)
		renderContent(context, mouseX, mouseY, deltaTicks)
		renderOptionWidget(context, mouseX, mouseY, deltaTicks)
		drawScrollbar(context, mouseX, mouseY)
	}

	override fun setupSelectedPage(selected: Race){
		val y = y + headerHeight
		selectedPage.add(
			TraitEntry.newOf(
				x + 20, y, width - 40, this, Text.translatable("mtt.creature_type"), 0, listOf(
					LiteralPart(
						"mtt.creature_type.declaration",
						Text.translatable("mtt.creature_type.${selected.type.name.lowercase()}")
					)
		)))
		selectedPage.add(
			TraitEntry.newOf(
				x + 20,
				y + 15,
				width - 40,
				this,
				Text.translatable("mtt.size"),
				1,
				selected.size.getParts()
			)
		)
		selectedPage.add(
			TraitEntry.newOf(
				x + 20, y + 30, width - 40, this, Text.translatable("mtt.speed"), 2, listOf(
					LiteralPart("mtt.speed.declaration", "${selected.speed} ft"),
		)))
		selectedPage.add(
			TraitEntry.newOf(
				x + 20, y + 45, width - 40, this, Text.translatable("mtt.model"), 3, listOf(
					OneChoicePart(
						selected.models,
						Text.translatable("mtt.model"),
						selected::getModel,
						CharacterModel::id,
						{ "mtt.model.${it.id}" },
						"mtt.model.declaration",
						{ sheet, m, _ -> sheet.model = m })
				)
			)
		)

		var i = 4
		if (selected.subraces.isNotEmpty()) {
			val declaration = SubracePart(selected, this)
			val parts = setOf(declaration, LiteralPart("mtt.subraces.description"))
			selectedPage.add(
				TraitEntry.newOf(
					x + 20,
					y + 60,
					width - 40,
					this,
					Text.translatable("mtt.subraces"),
					i++,
					parts
				)
			)
		}

		selected.traits.forEach { trait ->
			selectedPage.add(TraitEntry.newOf(x + 20, y + 15 * i, width - 40, this, trait.getLabel(), i++, trait.getParts()))
		}
		selected.namedTraits.forEach { namedTrait ->
			selectedPage.add(
				TraitEntry.newOf(
					x + 20,
					y + 15 * i,
					width - 40,
					this,
					Text.translatable("mtt.trait.${namedTrait.name}"),
					i++,
					namedTrait.traits.flatMap { it.getParts() }.toSet()
				)
			)
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
			context.drawTexture(
				RenderPipelines.GUI_TEXTURED,
				Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/race_icons/${value.id}.png"),
				(x / scale).toInt(),
				(y / scale).toInt(),
				0f,
				0f,
				8,
				8,
				8,
				8
			)
			context.drawText(
				textRenderer,
				Text.translatable("mtt.race.${value.id}"),
				(x / scale).toInt() + 10,
				(y / scale).toInt() + 1,
				MTTScreens.textColour,
				false
			)
		}
	}

	class SubracePart(private val race: Race, val widget: RaceTabWidget) : OneChoicePart<Subrace>(
		race.subraces,
		Text.translatable("mtt.subraces"),
		{ str -> race.getSubrace(str)!! },
		Subrace::name,
		{ "mtt.race.${it.name}" },
		"mtt.subraces.declaration",
		{ sheet, subrace, _ -> sheet.subrace = subrace }) {

		private var lastSubraceName = ""

		override fun onSelect(value: Any?) {
			super.onSelect(value)
			val subrace = value as? Subrace
			val subraceID = subrace?.name ?: ""

			// Update Server
			val payload = ClickButtonC2SPayload("subrace", subraceID)
			ClientPlayNetworking.send(payload)

			if (lastSubraceName != subraceID) {
				// Remove previous subrace's traits
				widget.removeConditionalTraits("subrace_$lastSubraceName")

				// Add new subrace's traits
				if (subrace != null) {
					val entries = mutableSetOf<TraitEntry>()
					val x = widget.x + 20
					val y = widget.y + widget.headerHeight + widget.contentsHeightWithPadding
					val w = widget.width - 40
					var i = 0
					for (trait in subrace.traits) {
						entries.add(
							TraitEntry.newOf(
								x,
								y + 15 * i,
								w,
								widget,
								"subrace",
								trait.getLabel(),
								i++,
								trait.getParts()
							)
						)
					}
					for (namedTrait in subrace.namedTraits) {
						entries.add(
							TraitEntry.newOf(
								x,
								y + 15 * i,
								w,
								widget,
								"subrace",
								Text.translatable("mtt.feature.${namedTrait.name}"),
								i++,
								namedTrait.traits.flatMap { it.getParts() }.toSet()
							)
						)
					}
					widget.addConditionalTraits("subrace_${subrace.name}", entries)
				}
				lastSubraceName = subraceID
			}
		}
	}
}