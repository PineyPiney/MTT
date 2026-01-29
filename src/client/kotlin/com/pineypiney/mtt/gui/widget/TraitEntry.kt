package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.*
import com.pineypiney.mtt.network.payloads.c2s.UpdateTraitC2SPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
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

class TraitEntry(x: Int, y: Int, width: Int, val tab: CharacterCreatorOptionsTabWidget<*>, private val label: Text, private val index: Int, private val segments: List<Segment>): ClickableWidget(x, y, width, 13, Text.literal("Trait Entry ${label.string}")){

	val src = when(tab){
		is RaceTabWidget -> "race"
		is ClassTabWidget -> "class"
		is BackgroundTabWidget -> "background"
		else -> "null"
	}
	// The absolute value is the opening time and the input of the easing function,
	// if positive it is opening and if negative it is closing
	private var openness = 0f

	private var hoveredSegment = -1

	val isReady get() = segments.all { it.isReady }

	//val isReady = segments.

	init {
		segments.forEach { it.init(this) }
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		updateEase(deltaTicks * .2f)

		// Render the name of the entry
		context.drawText(tab.client.textRenderer, label, x + 2, y + 2, 4210752, false)

		if(!this.isReady){
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("textures/gui/sprites/icon/unseen_notification.png"), x + width - 20, y + 2, 0f, 0f, 8, 8, 8, 8)
		}

		// Render the Open/Close button
		DynamicWidgets.drawThinBox(context, x + width - 12, y, 12, 12, -3750202, -1, -11184811)

		context.matrices.push()
		val rotationQuat = Quaternionf(AxisAngle4f(DynamicWidgets.easeInOutBack(abs(openness)) * PI.toFloat(), 0f, 0f, 1f))
		context.matrices.multiply(rotationQuat, x + width - 6f, y + 6f, 0f)
		context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/sprites/widget/button_icon.png"), x + width - 10, y + 4, 0f, 0f, 8, 4, 16, 16)
		context.matrices.pop()

		// Render the content
		if(openness != 0f) {

			DynamicWidgets.drawThickBox(context, x, y + 13, width, getBoxHeight())

			context.enableScissor(x + 3, y + 16, x + width - 3, y + 10 + getBoxHeight() )

			val x = x + 5
			var y = y + 18
			val shadow = tab.optionSelectWidget == null
			val hoveringX = mouseX >= x && mouseX < x + width - 10

			val scrollTop = tab.y + tab.headerHeight
			val scrollBottom = tab.y + tab.height - tab.footerHeight

			hoveredSegment = -1
			for(i in 0..<segments.size){
				val segment = segments[i]
				if(hoveringX && mouseY >= y && mouseY > scrollTop && mouseY < y + segment.height && mouseY < scrollBottom) hoveredSegment = i
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

	interface Segment{
		val height: Int
		val isReady: Boolean
		fun init(entry: TraitEntry) {}
		fun render(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int, i: Int, entry: TraitEntry, shadow: Boolean)
		fun onClick(mouseX: Double, mouseY: Double, entry: TraitEntry) = true
		fun apply(sheet: CharacterSheet, source: Source){}
	}

	interface TextSegment : Segment{
		override val height: Int get() = getNumLines * 9
		val getNumLines: Int
	}

	class LiteralTextSegment(val lines: List<Text>): TextSegment{
		override val getNumLines: Int get() = lines.size
		override val isReady: Boolean = true
		override fun render(
			ctx: DrawContext,
			x: Int,
			y: Int,
			mouseX: Int,
			mouseY: Int,
			i: Int,
			entry: TraitEntry,
			shadow: Boolean
		) {
			var lineY = y
			for(line in lines){
				ctx.drawText(entry.tab.client.textRenderer, line, x, lineY, 16777215, shadow)
				lineY += 9
			}
		}
	}

	class OneChoiceSegment<T>(val part: OneChoicePart<T>, val index: Int): TextSegment{
		val traitLines  = mutableListOf<OrderedText>()
		override val getNumLines: Int get() = 1 + traitLines.size
		override val isReady: Boolean get() = part.isReady()

		fun generateTraitLines(textRenderer: TextRenderer, width: Int){
			traitLines.clear()
			val currentDecision = part.decision
			if(currentDecision != null) {
				val text = Text.translatable(part.declarationKey, Text.translatable(part.translationKey(currentDecision)), *part.args)
				traitLines.addAll(textRenderer.wrapLines(text, width))
			}
		}

		override fun render(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int, i: Int, entry: TraitEntry, shadow: Boolean) {
			val colour = if (entry.hoveredSegment == i && entry.tab.optionSelectWidget == null) 6791374 else 16777215
			val pickText = if(isReady) Text.translatable("mtt.trait.change_answer") else Text.translatable("mtt.trait.pick_a", part.label)
			ctx.drawText(entry.tab.client.textRenderer, pickText, x, y, colour, shadow)
			var lineY = y + 9
			for(line in traitLines){
				ctx.drawText(entry.tab.client.textRenderer, line, x, lineY, 16777215, shadow)
				lineY += 9
			}
		}

		override fun onClick(mouseX: Double, mouseY: Double, entry: TraitEntry): Boolean {
			val height = entry.tab.client.currentScreen?.height ?: return false
			entry.tab.optionSelectWidget = OptionsSelectWidget(entry.tab.client.textRenderer, part.label, listOfNotNull(part.decision), 1, part.choices.toList(), { Text.translatable(part.translationKey(it)) }, entry.x + (entry.width / 2) - 75, height / 2, 150, 100, Text.literal("OptionSelect")){
				part.decision = it.firstOrNull()
				generateTraitLines(entry.tab.client.textRenderer, entry.width - 10)
				entry.height = 13 + entry.getBoxHeight()
				entry.tab.reposition(entry.index)
				entry.tab.optionSelectWidget = null
				ClientPlayNetworking.send(UpdateTraitC2SPayload(entry.src, entry.index, index, it.map(part.unparse)))
			}
			return true
		}

		override fun apply(sheet: CharacterSheet, source: Source) {
			//part.applyWithValues(sheet, source)
		}
	}

	class GiveNOptsSegment<T>(val part: GivenAndOptionsPart<T>, val index: Int): TextSegment{
		val traitLines  = mutableListOf<OrderedText>()
		override val getNumLines: Int get() = 1 + traitLines.size
		override val isReady: Boolean get() = part.isReady()

		override fun init(entry: TraitEntry) {
			generateTraitLines(entry.tab.client.textRenderer, entry.width)
		}

		fun generateTraitLines(textRenderer: TextRenderer, width: Int){
			traitLines.clear()
			if(part.decisions.isNotEmpty() || part.data.given.isNotEmpty()) {
				val text = part.getCurrentList()
				traitLines.addAll(textRenderer.wrapLines(Text.translatable(part.declarationKey, text, *part.args), width))
			}
		}

		override fun render(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int, i: Int, entry: TraitEntry, shadow: Boolean) {
			val colour = if (entry.hoveredSegment == i && entry.tab.optionSelectWidget == null) 6791374 else 16777215

			val pickText = if(part.isReady()) Text.translatable("mtt.trait.change_answer")
			else {
				val remaining = part.data.numChoices - part.decisions.size
				if(remaining == 1) Text.translatable("mtt.trait.pick_a", part.label)
				else Text.translatable("mtt.trait.pick_n", part.label, remaining)
			}
			ctx.drawText(entry.tab.client.textRenderer, pickText, x, y, colour, shadow)
			var lineY = y + 9
			for(line in traitLines){
				ctx.drawText(entry.tab.client.textRenderer, line, x, lineY, 16777215, shadow)
				lineY += 9
			}
		}

		override fun onClick(mouseX: Double, mouseY: Double, entry: TraitEntry): Boolean {
			val height = entry.tab.client.currentScreen?.height ?: return false
			entry.tab.optionSelectWidget = OptionsSelectWidget(entry.tab.client.textRenderer, part.label, part.decisions, part.data.numChoices, part.data.options.toList(), { Text.translatable(part.translationKey(it)) }, entry.x + (entry.width / 2) - 75, height / 2, 150, 100, Text.literal("OptionSelect")){
				part.decisions.clear()
				part.decisions.addAll(it)
				
				generateTraitLines(entry.tab.client.textRenderer, entry.width - 10)
				entry.height = 13 + entry.getBoxHeight()
				entry.tab.reposition(entry.index)

				entry.tab.optionSelectWidget = null
				ClientPlayNetworking.send(UpdateTraitC2SPayload(entry.src, entry.index, index, it.map(part.unparse)))
			}
			return true
		}

		override fun apply(sheet: CharacterSheet, source: Source) {
			//trait.applyWithValues(sheet, source)
		}
	}

	class TallySegment<T>(val part: TallyPart<T>, val index: Int) : Segment {

		val labels = part.options.map { Text.translatable(part.translationKey(it)) }
		override val height: Int get() = 9 + 12 * labels.size
		override val isReady: Boolean get() = part.isReady()

		var hoveredIcon = -1

		override fun render(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int, i: Int, entry: TraitEntry, shadow: Boolean) {
			val textRenderer = entry.tab.client.textRenderer
			val labelWidth = labels.maxOf { textRenderer.getWidth(it) } + 20

			val pointsText = Text.literal("${part.pointsLeft}/${part.points}")
			ctx.drawText(textRenderer, pointsText, x + labelWidth + 5 * part.points + 3 - textRenderer.getWidth(pointsText) / 2, y, 16777215, shadow)

			hoveredIcon = -1
			for((i, value) in part.tallies.withIndex()){
				val buttonY = y + 12 * (i + 1)
				val hoveringRow = mouseY >= buttonY && mouseY < buttonY + 7
				ctx.drawText(textRenderer, labels[i], x + labelWidth - textRenderer.getWidth(labels[i]), buttonY, 16777215, shadow)
				for(j in 0..<part.points){
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
			}
		}

		override fun onClick(mouseX: Double, mouseY: Double, entry: TraitEntry): Boolean {
			if(hoveredIcon != -1){
				val row = hoveredIcon shr 8
				val index = hoveredIcon and 255
				val score = part.tallies[row]
				val adding = index >= score
				if(adding && part.pointsLeft > 0) part.tallies[row]++
				else if(!adding && part.tallies[row] > 0) part.tallies[row]--

				val list = mutableListOf<String>()
				var i = 0
				for(option in part.options){
					val str = part.unparse(option)
					val pointsInOption = part.tallies[i++]
					for(j in 1..pointsInOption) list.add(str)
				}
				ClientPlayNetworking.send(UpdateTraitC2SPayload(entry.src, entry.index, this.index, list))
				return true
			}
			return false
		}
	}

	companion object {
		fun newOf(x: Int, y: Int, width: Int, tab: CharacterCreatorOptionsTabWidget<*>, label: Text, index: Int, parts: Collection<TraitPart>): TraitEntry{
			val segments: List<Segment> = parts.mapIndexed { partIndex, part ->
				when (part) {
					is LiteralPart -> LiteralTextSegment(tab.client.textRenderer.textHandler.wrapLines(part.text, width - 10, Style.EMPTY)
						.map { line -> Text.literal(line.string) })

					is OneChoicePart<*> -> OneChoiceSegment(part, partIndex)
					is GivenAndOptionsPart<*> -> GiveNOptsSegment(part, partIndex)
					is TallyPart<*> -> TallySegment(part, partIndex)
				}
			}
			return TraitEntry(x, y, width, tab, label, index, segments)
		}
	}
}