package com.pineypiney.mtt.gui.widget

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Source
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class BackgroundTabWidget(sheet: CharacterSheet, client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, message: Text, backgrounds: Set<Background>) : CharacterCreatorOptionsTabWidget<Background>(sheet, client, x, y, width, height, message) {

	override val valueSelectChildren: List<Entry<Background>> = backgrounds.map {
		BackgroundEntry(it, this, 8, 32, 240, 20, Text.literal(it.id))
	}

	override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		renderSelectedTitle(context, "mtt.background.${selected?.id}", mouseX, mouseY)
		renderContent(context, mouseX, mouseY, deltaTicks)
		renderOptionWidget(context, mouseX, mouseY, deltaTicks)
		drawScrollbar(context)
	}

	override fun setupSelectedPage(selected: Background) {
		val x = x + 20
		val w = width - 40
		var i = 0
		for(trait in selected.compileTraits()){
			selectedPage.add(TraitEntry.newOf(x, y + 25 + 15 * i, w, this, trait.getLabel(), i++, trait.getParts()))
		}
	}

	override fun apply(sheet: CharacterSheet) {
		val src = Source.BackgroundSource(selected ?: return)
		for(trait in selectedPage){
			trait.updateValues(sheet, src)
		}
	}

	class BackgroundEntry(background: Background, tab: BackgroundTabWidget, x: Int, y: Int, width: Int, height: Int, message: Text): Entry<Background>(background, tab, x, y, width, height, message){

		override val type: String = "background"
		override fun getID(value: Background): String = value.id

		override fun render(context: DrawContext, textRenderer: TextRenderer, x: Int, y: Int, scale: Float) {
			setPosition(x, y)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/background_icons/${value.id}.png"), (x / scale).toInt(), (y / scale).toInt(), 0f, 0f, 8, 8, 8, 8)
			context.drawText(textRenderer, Text.translatable("mtt.background.${value.id}"), (x / scale).toInt() + 10, (y / scale).toInt() + 1, 4210752, false)
		}
	}
}