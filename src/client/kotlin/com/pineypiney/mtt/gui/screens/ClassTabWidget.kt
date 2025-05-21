package com.pineypiney.mtt.gui.screens

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.species.Species
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ContainerWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayers
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class ClassTabWidget(val client: MinecraftClient, x: Int, y: Int, width: Int, height: Int, message: Text, private val species: List<Species>) : ContainerWidget(x, y, width, height, message) {

	val speciesSelectChildren = species.map { TextWidget(8, 32, 240, 30, Text.literal(it.id), client.textRenderer) }

	override fun getContentsHeightWithPadding(): Int {
		return species.size * 30
	}

	override fun getDeltaYPerScroll(): Double {
		return 15.0
	}

	override fun children(): List<Element?>? {
		return speciesSelectChildren
	}

	override fun renderWidget(
		context: DrawContext,
		mouseX: Int,
		mouseY: Int,
		deltaTicks: Float
	) {
		context.enableScissor(x, y, right, bottom)
		val s = 3f
		for(i in 0..<speciesSelectChildren.size){
			val entryY = ((y + 30 + i * 30) / s).toInt()
			val entryX = ((x + 50) / s).toInt()
			context.matrices.push()
			context.matrices.scale(s, s, s)
			context.drawTexture(RenderLayer::getGuiTextured, Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/species_icons/${species[i].id}.png"), entryX, entryY, 0f, 0f, 8, 8, 8, 8)
			context.drawText(client.textRenderer, Text.translatable("mtt.species.${species[i].id}"), entryX + 10, entryY - 1, 4210752, false)

			//speciesSelectChildren[i].renderWidget(context, mousex)
			context.matrices.pop()
		}
		context.disableScissor()
		drawScrollbar(context)
	}

	override fun appendClickableNarrations(builder: NarrationMessageBuilder?) {

	}
}