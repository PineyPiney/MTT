package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.CharacterSheet
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitOption
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min

class SpeciesTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, val yOrigin: Int, width: Int, val panelHeight: Int, message: Text, private val species: List<Species>) : CharacterCreatorTabWidget(
	sheet, client,
	x,
	yOrigin,
	width,
	panelHeight,
	message
) {

	val speciesSelectChildren = species.mapIndexed { i, species -> SpeciesEntry(species, 0, 0, width - 100, 24, 3f, Text.literal("Species Entry")){ species ->
		selectedSpecies = species
	} }

	private var selectedSpecies: Species? = null
		set(value) {
			if(field != value) {
				field = value
				speciesPage.clear()
				if(value != null) {
					yOffset = 25
					y = yOrigin + yOffset
					height = panelHeight - yOffset
					setupSpeciesPageNew(value)
				}
				else {
					yOffset = 0
					y = yOrigin
					height = panelHeight
					isBackButtonHovered = false
					speciesPage.clear()
				}
			}
		}
	var isBackButtonHovered = false

	val speciesPage: MutableList<ClickableWidget> = mutableListOf()

	var optionSelectWidget: OptionsSelectWidget<*>? = null

	override fun getContentsHeightWithPadding(): Int {
		return if(selectedSpecies == null) speciesSelectChildren.size * 30
		else speciesPage.sumOf { it.height + 2 }
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		// If there is a pop up options widget then nothing else should be interactable
		if(optionSelectWidget != null) {
			return optionSelectWidget?.mouseClicked(mouseX, mouseY, button) == true
		}
		if(isBackButtonHovered) {
			optionSelectWidget = null
			selectedSpecies = null
			speciesPage.clear()
			return true
		}
		return super.mouseClicked(mouseX, mouseY, button)
	}

	override fun getDeltaYPerScroll(): Double {
		return 15.0
	}

	override fun mouseScrolled(
		mouseX: Double,
		mouseY: Double,
		horizontalAmount: Double,
		verticalAmount: Double
	): Boolean {
		super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
		if(optionSelectWidget == null) reposition()
		return true
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
		if(selectedSpecies != null){
			val titleText = Text.translatable("mtt.species.${selectedSpecies?.id}")
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

		optionSelectWidget?.let { widget ->
			context.fill(0, 0, client.currentScreen!!.width, client.currentScreen!!.height, 0, 2050307381)
			context.enableScissor(widget.x, widget.y, widget.x + widget.width, widget.y + widget.height)
			widget.render(context, mouseX, mouseY, deltaTicks)
			context.disableScissor()
		}
		drawScrollbar(context)
	}

	override fun drawScrollbar(context: DrawContext) {
		if(overflows()){
			DynamicWidgets.drawThinBox(context, x + width - 14, y, 14, height, -7631989, -13158601)
			DynamicWidgets.drawScroller(context, x + width - 13, scrollbarThumbY + 1, 12, scrollbarThumbHeight - 2)
		}
	}

	override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
		return active && visible && mouseX >= x && mouseY >= yOrigin && mouseX < right && mouseY < yOrigin + panelHeight
	}

	fun setupSpeciesPageNew(species: Species){
		speciesPage.add(TraitEntry(x + 20, y, width - 40, this, "creature_type", 0, SetTraits(species.type), {"mtt.creature_type.${it.name.lowercase()}"}){ values ->
			sheet.type = values.firstOrNull() ?: return@TraitEntry
		})
		speciesPage.add(TraitEntry(x + 20, y + 15, width - 40, this, "size", 1, species.size, {"mtt.size.${it.name}"}){ values ->
			sheet.size = values.firstOrNull() ?: return@TraitEntry
		})
		speciesPage.add(TraitEntry(x + 20, y + 30, width - 40, this, "speed", 2, SetTraits(species.speed), {"$it ft"}){ values ->
			sheet.speed = values.firstOrNull() ?: return@TraitEntry
		})
		speciesPage.add(TraitEntry(x + 20, y + 45, width - 40, this, "model", 3, species.model, {"mtt.model.$it"}) { values ->
			sheet.model = values.firstOrNull() ?: return@TraitEntry
		})
		species.components.forEachIndexed { i, comp ->
			//speciesPage.add(TraitEntry(x + 20, y + 65 + 15 * i, width - 40, this, comp.getID(), i + 4, comp.))
		}
	}

	override fun reposition(start: Int) {
		optionSelectWidget?.let {
			val height = client.currentScreen?.height ?: return
			// Place the options picker in the middle of the screen
			it.x = x + (width - it.width) / 2
			it.y = (height - it.height) / 2
		}

		var (y, range) = when (start) {
			-1 -> (y - scrollY.toInt()) to 0..<speciesPage.size
			else -> speciesPage[start].y + speciesPage[start].height + 2 to (start + 1)..<speciesPage.size
		}
		for (i in range) {
			val entry = speciesPage[i]
			entry.x = x + 20
			entry.y = y
			y += entry.height + 2
		}
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}

	class TraitEntry<T>(x: Int, y: Int, width: Int, val tab: SpeciesTabWidget, id: String, val index: Int, val trait: Trait<T>, val translateKey: (T) -> String, val updateSheet: (values: List<T>) -> Unit): ClickableWidget(x, y, width, 13, Text.literal("Trait Entry $id")){

		val label = Text.translatable("mtt.trait.$id")
		val pickText = Text.translatable("mtt.trait.pick_a", label)
		val description = Text.translatable("mtt.trait.$id.description")
		val descriptionLines = tab.client.textRenderer.textHandler.wrapLines(description, width - 10, Style.EMPTY).map { Text.literal(it.string) }

		// The absolute value is the opening time and the input of the easing function,
		// if positive it is opening and if negative it is closing
		var openness = 0f

		var hoveringPickButton = false
		val decisions: MutableList<T?> = when(trait){
			is SetTraits -> trait.values.toMutableList()
			is TraitOption -> MutableList(trait.choices){ null }
			else -> mutableListOf()
		}

		init {
			updateValues()
		}

		override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
			updateEase(deltaTicks * .2f)

			context.drawText(tab.client.textRenderer, label, x + 2, y + 2, 4210752, false)
			DynamicWidgets.drawThinBox(context, x + width - 12, y, 12, 12, -3750202, -1, -11184811)

			context.matrices.push()
			val rotationQuat = Quaternionf(AxisAngle4f(DynamicWidgets.easeInOutBack(abs(openness)) * PI.toFloat(), 0f, 0f, 1f))
			context.matrices.multiply(rotationQuat, x + width - 6f, y + 6f, 0f)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/button_icon.png"), x + width - 10, y + 4, 0f, 0f, 8, 4, 16, 16)
			context.matrices.pop()

			if(openness != 0f) {

				DynamicWidgets.drawThickBox(context, x, y + 13, width, getBoxHeight())

				context.enableScissor(x + 3, y + 16, x + width - 3, y + 10 + getBoxHeight() )

				//context.drawText(tab.client.textRenderer, dec, x + 5, y + 18, -1, true)

				val x = x + 5
				var y = y + 18
				val shadow = tab.optionSelectWidget == null

				hoveringPickButton = mouseX >= x && mouseY >= y && mouseX < x + width - 10 && mouseY < y + decisions.size * 9
				for(i in 0..<decisions.size){
					val value = decisions[i]
					if(value != null) context.drawText(tab.client.textRenderer, Text.translatable(translateKey(value)), x, y, 16777215, shadow)
					else context.drawText(tab.client.textRenderer, pickText, x, y, if (hoveringPickButton && tab.optionSelectWidget == null) 6791374 else 16777215, shadow)
					y += 9
				}
//				when(trait){
//					is SetTraits<T> -> context.drawText(tab.client.textRenderer, trait.values.joinToString{ Text.translatable(translateKey(it)).string }, x + 5, y + 18, -1, true)
//					is TraitOption<T> -> {
//						if(trait.choices == 1) context.drawText(tab.client.textRenderer, Text.translatable("mtt.trait.pick_your", label), x + 5, y + 18, -1, true)
//					}
//				}
				y += 3
				descriptionLines.forEach {
					context.drawText(tab.client.textRenderer, it, x, y, 16777215, shadow)
					y += 9
				}
				context.disableScissor()
			}
		}

		fun updateEase(delta: Float){
			openness = if(openness == 0f || openness == 1f) return
			else if(openness < 0f) min(openness + delta, 0f)
			else min(openness + delta, 1f)
			height = 13 + getBoxHeight()
			val oldScroll = tab.scrollY
			tab.scrollY = tab.scrollY
			tab.reposition(if(oldScroll == tab.scrollY) index else -1)
		}

		fun easedValue() = (1 - cos(openness * PI.toFloat())) * .5f

		fun getBoxHeight(): Int {
			val full = if(descriptionLines.isEmpty()) 9 + 9 * decisions.size
			else 12 + (9 * (descriptionLines.size + decisions.size))
			return (full * easedValue()).toInt()
		}

		override fun onClick(mouseX: Double, mouseY: Double) {
			if(hoveringPickButton && trait is TraitOption){
				val height = tab.client.currentScreen?.height ?: return
				tab.optionSelectWidget = OptionsSelectWidget(tab.client.textRenderer, pickText, decisions, trait.options, { Text.translatable(translateKey(it)) }, x + (width / 2) - 43, height / 2, 86, 100, Text.literal("OptionSelect")){
					for(i in 0..<decisions.size){
						if(it.size > i) decisions[i] = it[i]
						else decisions[i] = null
					}
					tab.optionSelectWidget = null
				}
				return
			}
			val r = x + width
			if(mouseX >= r - 12 && mouseX < r && mouseY >= y && mouseY < y + 12){
				openness = if(openness == 0f) 0.01f
				else -openness
			}
		}

		fun updateValues(){
			updateSheet(decisions.filterNotNull())
		}

		override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

		}
	}

	class SpeciesEntry(val species: Species, x: Int, y: Int, width: Int, height: Int, val renderScale: Float, message: Text, val onClick: (Species) -> Unit): ClickableWidget(x, y, width, height, message){
		override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {

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