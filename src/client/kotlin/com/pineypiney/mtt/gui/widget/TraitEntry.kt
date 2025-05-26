package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitOption
import com.pineypiney.mtt.util.Localisation
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min

class TraitEntry<T>(x: Int, y: Int, width: Int, val tab: CharacterCreatorOptionsTabWidget<*>, private val label: Text, private val index: Int, private val segments: List<Segment<T>>): ClickableWidget(x, y, width, 13, Text.literal("Trait Entry ${label.string}")){

	private val pickText = Text.translatable("mtt.trait.pick_a", label)

	// The absolute value is the opening time and the input of the easing function,
	// if positive it is opening and if negative it is closing
	private var openness = 0f

	private var hoveredSegment = -1

	val isReady = segments.all { if(it is TraitSegment) it.trait.isReady else true }

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

			val x = x + 5
			var y = y + 18
			val shadow = tab.optionSelectWidget == null
			val hoveringX = mouseX >= x && mouseX < x + width - 10

			hoveredSegment = -1
			for(i in 0..<segments.size){
				val segment = segments[i]
				if(hoveringX && mouseY >= y && mouseY < y + segment.height) hoveredSegment = i
				segment.render(context, x, y, mouseX, mouseY, i, this, shadow)
				y += segment.height + 3
			}

			context.disableScissor()
		}
	}

	private fun updateEase(delta: Float){
		openness = if(openness == 0f || openness == 1f) return
		else if(openness < 0f) min(openness + delta, 0f)
		else min(openness + delta, 1f)
		height = 13 + getBoxHeight()
		val oldScroll = tab.scrollY
		tab.scrollY = tab.scrollY
		tab.reposition(if(oldScroll == tab.scrollY) index else -1)
	}

	private fun easedValue() = (1 - cos(openness * PI.toFloat())) * .5f

	private fun getBoxHeight(): Int {
		val full = 9 + segments.sumOf { it.height } + (segments.size - 1) * 3
		return (full * easedValue()).toInt()
	}

	override fun onClick(mouseX: Double, mouseY: Double) {
		val segment = segments.getOrNull(hoveredSegment)
		if(segment?.onClick(mouseX, mouseY, this) == true) return

		val r = x + width
		if(mouseX >= r - 12 && mouseX < r && mouseY >= y && mouseY < y + 12){
			openness = if(openness == 0f) 0.01f
			else -openness
		}
	}

	fun updateValues(sheet: CharacterSheet, src: Source){
		for(segment in segments){
			segment.apply(sheet, src)
		}
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}

	data class FormattedTrait<T>(val formatKey: String, val trait: Trait<T>)

	interface Segment<T>{
		val height: Int
		fun render(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int, i: Int, entry: TraitEntry<T>, shadow: Boolean)
		fun onClick(mouseX: Double, mouseY: Double, entry: TraitEntry<T>) = true
		fun apply(sheet: CharacterSheet, source: Source){}
	}

	interface TextSegment<T> : Segment<T>{
		override val height: Int get() = getNumLines * 9
		val getNumLines: Int
	}

	class LiteralTextSegment<T>(val lines: List<Text>): TextSegment<T>{
		override val getNumLines: Int get() = lines.size
		override fun render(
			ctx: DrawContext,
			x: Int,
			y: Int,
			mouseX: Int,
			mouseY: Int,
			i: Int,
			entry: TraitEntry<T>,
			shadow: Boolean
		) {
			var lineY = y
			for(line in lines){
				ctx.drawText(entry.tab.client.textRenderer, line, x, lineY, 16777215, shadow)
				lineY += 9
			}
		}
	}
	class TraitSegment<T>(val formatKey: String, val translateKey: (T) -> String, val trait: TraitOption<T>): TextSegment<T>{
		val decisions = MutableList<T?>(trait.choices){ null }
		val traitLines  = mutableListOf<OrderedText>()
		override val getNumLines: Int get() = 1 + traitLines.size

		fun generateTraitLines(textRenderer: TextRenderer, width: Int){
			val currentDecisions = decisions.filterNotNull()
			traitLines.clear()
			if(currentDecisions.isNotEmpty()) {
				val text = Localisation.translateList(currentDecisions, false, translateKey)
				traitLines.addAll(textRenderer.wrapLines(Text.translatable(formatKey, text), width))
			}
		}

		override fun render(
			ctx: DrawContext,
			x: Int,
			y: Int,
			mouseX: Int,
			mouseY: Int,
			i: Int,
			entry: TraitEntry<T>,
			shadow: Boolean
		) {
			val colour = if (entry.hoveredSegment == i && entry.tab.optionSelectWidget == null) 6791374 else 16777215
			ctx.drawText(entry.tab.client.textRenderer, entry.pickText, x, y, colour, shadow)
			var lineY = y + 9
			for(line in traitLines){
				ctx.drawText(entry.tab.client.textRenderer, line, x, lineY, 16777215, shadow)
				lineY += 9
			}
		}

		override fun onClick(mouseX: Double, mouseY: Double, entry: TraitEntry<T>): Boolean {
			val height = entry.tab.client.currentScreen?.height ?: return false
			entry.tab.optionSelectWidget = OptionsSelectWidget(entry.tab.client.textRenderer, entry.pickText, decisions, trait.options, { Text.translatable(translateKey(it)) }, entry.x + (entry.width / 2) - 75, height / 2, 150, 100, Text.literal("OptionSelect")){
				for(i in 0..<decisions.size){
					if(it.size > i) decisions[i] = it[i]
					else decisions[i] = null
				}
				generateTraitLines(entry.tab.client.textRenderer, entry.width - 10)
				entry.height = 13 + entry.getBoxHeight()
				entry.tab.reposition(entry.index)

				entry.tab.optionSelectWidget = null
			}
			return true
		}

		override fun apply(sheet: CharacterSheet, source: Source) {
			trait.applyWithValues(sheet, source)
		}
	}

	companion object {
		fun <T> of(x: Int, y: Int, width: Int, tab: CharacterCreatorOptionsTabWidget<*>, label: Text, index: Int, parts: List<Any>, translateKey: (T) -> String): TraitEntry<T>{
			@Suppress("UNCHECKED_CAST")
			val segments: List<Segment<T>> = parts.mapNotNull { part ->
				when (part) {
					is Text -> LiteralTextSegment(tab.client.textRenderer.textHandler.wrapLines(part, width - 10, Style.EMPTY)
						.map { line -> Text.literal(line.string) })

					is FormattedTrait<*> -> {
						when (part.trait) {
							is SetTraits<*> -> {
								try {
									val string = Text.translatable(part.formatKey, Localisation.translateList((part.trait.values as Collection<T>).toList(), false, translateKey))
									LiteralTextSegment(tab.client.textRenderer.textHandler.wrapLines(string, width - 10, Style.EMPTY)
										.map { line -> Text.literal(line.string) })
								} catch (e: Exception) {
									MTT.logger.warn("Couldn't make LineSegment from trait ${part.trait}:\n${e.message}")
									null
								}
							}

							is TraitOption<*> -> {
								try { TraitSegment(part.formatKey, translateKey, part.trait as TraitOption<T>) } catch (e: Exception){ null }

							}

							else -> null
						}
					}
					else -> null
				}
			}
			return TraitEntry(x, y, width, tab, label, index, segments)
		}
	}
}