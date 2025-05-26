package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.SetTraits
import com.pineypiney.mtt.dnd.traits.TraitOption
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import kotlin.math.PI

class ClassTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, message: Text, private val classes: List<DNDClass>) : CharacterCreatorOptionsTabWidget<DNDClass>(sheet, client, x, y, width, height, message) {

	override val valueSelectChildren = classes.map { ClassEntry(it, 8, 32, 240, 20, Text.literal(it.id)){ clazz ->
		selected = clazz
	} }

	override fun renderWidget(
		context: DrawContext,
		mouseX: Int,
		mouseY: Int,
		deltaTicks: Float
	) {
		if(selected != null){
			val titleText = Text.translatable("mtt.class.${selected?.id}")
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
			val entryX = x + 50
			for (i in 0..<classes.size) {
				val entryY = (y + 30 + i * 30)
				valueSelectChildren[i].render(context, client.textRenderer, entryX, entryY)
			}
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

	override fun setupSelectedPage(selected: DNDClass) {
		val x = x + 20
		var i = 0
		val w = width - 40
		for(trait in selected.coreTraits){
			val type = when (trait) {
				is TraitOption -> (trait.options.firstOrNull() as? Proficiency)?.type
				is SetTraits -> (trait.values.firstOrNull() as? Proficiency)?.type
				else -> null
			} ?: "skill"
			val formatted = TraitEntry.FormattedTrait("mtt.feature.proficiency.declaration", trait)
			val entry = TraitEntry.of<Proficiency>(x, y + 15 * i, w, this, Text.translatable("mtt.feature.proficiency.$type"), i, listOf(formatted)){ proficiency ->
				"mtt.${proficiency.type}.${proficiency.id}"
			}
			selectedPage.add(entry)
			i++
		}
	}

	class ClassEntry(clazz: DNDClass, x: Int, y: Int, width: Int, height: Int, message: Text, onClick: (DNDClass) -> Unit): Entry<DNDClass>(clazz, x, y, width, height, message, onClick){

		override fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int){
			setPosition(x, y)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/class_icons/${value.id}.png"), x, y, 0f, 0f, 16, 16, 16, 16)
			context.matrices.push()
			context.matrices.scale(2f, 2f, 2f)
			context.drawText(textRenderer, Text.translatable("mtt.class.${value.id}"), (x / 2) + 10, (y / 2) + 1, 4210752, false)
			context.matrices.pop()
		}
	}
}