package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.traits.Source
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class ClassTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, message: Text, classes: List<DNDClass>) : CharacterCreatorOptionsTabWidget<DNDClass>(sheet, client, x, y, width, height, message) {

	override val valueSelectChildren = classes.map {
		ClassEntry(it, this, 8, 32, 240, 20, Text.literal(it.id))
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		renderSelectedTitle(context, "mtt.class.${selected?.id}", mouseX, mouseY)
		renderContent(context, mouseX, mouseY, deltaTicks)
		renderOptionWidget(context, mouseX, mouseY, deltaTicks)
		drawScrollbar(context)
	}

	override fun setupSelectedPage(selected: DNDClass) {
		val x = x + 20
		var i = 0
		val w = width - 40
		for(trait in selected.coreTraits){
			val entry = TraitEntry.newOf(x, y + 25 + 15 * i, w, this, Text.translatable(trait.getLabelKey()), i, trait.getParts())
			selectedPage.add(entry)
			i++
		}
	}

	override fun apply(sheet: CharacterSheet) {
		val src = Source.ClassSource(selected ?: return)
		for(trait in selectedPage){
			trait.updateValues(sheet, src)
		}
	}

	class ClassEntry(clazz: DNDClass, tab: ClassTabWidget, x: Int, y: Int, width: Int, height: Int, message: Text): Entry<DNDClass>(clazz, tab, x, y, width, height, message){

		override val type: String = "class"
		override fun getID(value: DNDClass): String = value.id

		override fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int, scale: Float){
			setPosition(x, y)
			context.matrices.scale(.5f, .5f, .5f)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/class_icons/${value.id}.png"), (x * 2f / scale).toInt(), (y * 2f / scale).toInt(), 0f, 0f, 16, 16, 16, 16)
			context.matrices.scale(2f, 2f, 2f)
			context.drawText(textRenderer, Text.translatable("mtt.class.${value.id}"), (x / scale).toInt() + 10, (y / scale).toInt() + 1, 4210752, false)
		}
	}
}