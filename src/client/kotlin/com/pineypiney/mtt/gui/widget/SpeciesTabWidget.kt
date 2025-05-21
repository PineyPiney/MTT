package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.Size
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.TraitOption
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SpeciesTabWidget(val client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, message: Text, private val species: List<Species>) : ContainerWidget(x, y, width, height, message) {

	val speciesSelectChildren = species.mapIndexed { i, species -> SpeciesEntry(species, 0, 0, width - 100, 24, 3f, Text.literal("Species Entry")){ species ->
		selectedSpecies = species
	} }

	private var selectedSpecies: Species? = null
		set(value) {
			if(field != value) {
				field = value
				speciesPage.clear()
				if(value != null) setupSpeciesPage(value)
			}
		}

	val speciesPage: MutableList<ClickableWidget> = mutableListOf()

	override fun getContentsHeightWithPadding(): Int {
		return speciesSelectChildren.size * 30
	}

	override fun getDeltaYPerScroll(): Double {
		return 15.0
	}

	override fun children(): List<Element> {
		return if(selectedSpecies == null) speciesSelectChildren else speciesPage
	}

	override fun renderWidget(
		context: DrawContext,
		mouseX: Int,
		mouseY: Int,
		deltaTicks: Float
	) {
		context.enableScissor(x, y, right, bottom)
		if(selectedSpecies == null) {
			val s = 3f
			context.matrices.push()
			context.matrices.scale(s, s, s)
			val entryX = x + 50
			for (i in 0..<species.size) {
				val entryY = (y + 30 + i * 30)
				speciesSelectChildren[i].render(context, client.textRenderer, entryX, entryY, i)
			}
			context.matrices.pop()
		}
		else {
			speciesPage.forEach { it.render(context, mouseX, mouseY, deltaTicks) }
		}
		context.disableScissor()
		drawScrollbar(context)
	}

	fun setupSpeciesPage(species: Species){
		val borderX = 20
		val x = this.x + borderX
		var y = this.y + 20
		val width = this.width - (2 * borderX)
		val midX = x + (width shr 1)

		speciesPage.add(TextWidget(x, y, width, 9, Text.translatable("mtt.species.${species.id}"), client.textRenderer))
		y += 10
		speciesPage.add(TextWidget(x, y, width, 9, Text.translatable("mtt.trait.creature_type").append(": ").append(Text.translatable("mtt.creature_type.${species.type}")), client.textRenderer))
		y += 10
		speciesPage.add(TextWidget(x, y, width, 9, Text.translatable("mtt.trait.speed").append(": ${species.movement}"), client.textRenderer))
		y += 10

		val sizeLabel = Text.translatable("mtt.trait.size").append(":")
		var labelWidth = client.textRenderer.getWidth(sizeLabel)
		speciesPage.add(TextWidget(midX - labelWidth, y, labelWidth, 9, sizeLabel, client.textRenderer))
		when(species.size){
			is SetTraits -> {
				val sizeText = Text.translatable("mtt.size.${(species.size as SetTraits<Size>).values.first().name}")
				labelWidth = client.textRenderer.getWidth(sizeText)
				speciesPage.add(TextWidget(midX + 2, y, labelWidth, 9, sizeText, client.textRenderer))
				y += 10
			}
			is TraitOption -> {
				val options = (species.size as TraitOption<Size>).options
				val sizeText = Text.translatable("mtt.size.${options.first().name}").append(", ").append(Text.translatable("mtt.size.${options[1].name}"))
				labelWidth = client.textRenderer.getWidth(sizeText)
				speciesPage.add(DropDownListWidget(x + 100 + 2, y, labelWidth, 12, options, client.textRenderer, { it -> Text.translatable("mtt.size.${it.name}") }, 12, Text.literal("Size Dropdown")))
				y += 14
			}
		}
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}

	class SpeciesEntry(val species: Species, x: Int, y: Int, width: Int, height: Int, val renderScale: Float, message: Text, val onClick: (Species) -> Unit): ClickableWidget(x, y, width, height, message){
		override fun renderWidget(
			context: DrawContext?,
			mouseX: Int,
			mouseY: Int,
			deltaTicks: Float
		) {

		}

		fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int, index: Int){
			setPosition(x, y)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/species_icons/${species.id}.png"), (x / renderScale).toInt(), (y / renderScale).toInt(), 0f, 0f, 8, 8, 8, 8)
			context.drawText(textRenderer, Text.translatable("mtt.species.${species.id}"), (x / renderScale).toInt() + 10, (y / renderScale).toInt() + 1, 4210752, false)

		}

		override fun onClick(mouseX: Double, mouseY: Double) {
			onClick(species)
		}

		override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

		}

	}
}