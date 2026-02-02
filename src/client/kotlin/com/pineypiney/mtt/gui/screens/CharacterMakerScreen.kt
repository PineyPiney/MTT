package com.pineypiney.mtt.gui.screens

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.gui.widget.*
import com.pineypiney.mtt.network.payloads.c2s.ClickButtonC2SPayload
import com.pineypiney.mtt.screen.CharacterMakerScreenHandler
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class CharacterMakerScreen(handler: CharacterMakerScreenHandler, playerInventory: PlayerInventory, title: Text) : HandledScreen<CharacterMakerScreenHandler>(handler, playerInventory, title) {

	var isReady = false
	val sheet = CharacterSheet()

	var tabOpen = 0
	val tabButtons = Array(4){ i ->
		ButtonWidget.builder(Text.literal("Funky Button!")) {
			remove(tabWidgets[tabOpen])
			tabOpen = i
			addDrawableChild(tabWidgets[i])
		}.position(x + i * 64, y).size(64, 28).build()
	}

	lateinit var tabWidgets: Array<CharacterCreatorTabWidget>

	init {
		backgroundWidth = 256
		backgroundHeight = 252
	}

	var doneButton = ButtonWidget.builder(Text.translatable("gui.done")) {
		val payload = ClickButtonC2SPayload("sheet_maker", "done")
		ClientPlayNetworking.send(payload)
	}.dimensions(x + 64, y + 222, 128, 24).build()

	override fun init() {
		super.init()

		tabWidgets = arrayOf(
			RaceTabWidget(sheet, client!!, x + 8, y + 32, 240, 188, Text.literal("Race Widget Text"), Race.set),
			ClassTabWidget(
				sheet,
				client!!,
				x + 8,
				y + 32,
				240,
				188,
				Text.literal("Class Widget Text"),
				DNDClass.classes
			),
			AbilitiesTabWidget(sheet, client!!, x + 8, y + 32, 240, 188, Text.literal("Abilities Widget Text")),
			BackgroundTabWidget(
				sheet,
				client!!,
				x + 8,
				y + 32,
				240,
				188,
				Text.literal("Background Widget Text"),
				Background.set
			)
		)
		addDrawableChild(tabWidgets[0])

		for((i, button) in tabButtons.withIndex()) {
			button.setPosition(x + i * 64, y)
			addSelectableChild(button)
		}
		doneButton.setPosition(x + 64, y + 222)
	}

	override fun refreshWidgetPositions() {
		this.x = (this.width - this.backgroundWidth) / 2
		this.y = (this.height - this.backgroundHeight) / 2
		tabButtons.forEachIndexed { i, button ->
			button.setPosition(x + i * 64, y)
		}
		tabWidgets.forEach {
			it.x = x + 8
			it.y = y + 32
			it.reposition()
		}
		doneButton.setPosition(x + 64, y + 222)
	}

	override fun mouseScrolled(
		mouseX: Double,
		mouseY: Double,
		horizontalAmount: Double,
		verticalAmount: Double
	): Boolean {
		for (element in children()) {
			if(element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true
		}
		return false
	}

	override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
		drawCenteredText(context, "mtt.character_maker_screen.race", 32, 16)
		drawCenteredText(context, "mtt.character_maker_screen.class", 96, 16)
		drawCenteredText(context, "mtt.character_maker_screen.abilities", 160, 16)
		drawCenteredText(context, "mtt.character_maker_screen.background", 224, 16)
	}

	override fun drawBackground(context: DrawContext, deltaTicks: Float, mouseX: Int, mouseY: Int) {
		checkReady()
		for(i in 0..3){
			if(i != tabOpen) {
				context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x + 64 * i, y, 192f, 0f, 64, 32, 256, 256)
			}
		}
		context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x, y + 28, 0f, 32f, 256, 224, 256, 256)

		when(tabOpen){
			0 -> context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x, y, 0f, 0f, 64, 32, 256, 256)
			1, 2 -> context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x + 64 * tabOpen, y, 64f, 0f, 64, 32, 256, 256)
			3 -> context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, x + 192, y, 128f, 0f, 64, 32, 256, 256)
		}
	}

	fun drawCenteredText(ctx: DrawContext, key: String, x: Int, y: Int){
		val text = Text.translatable(key)
		val width = textRenderer.getWidth(text)
		ctx.drawText(textRenderer, text, x - (width / 2), y, 4210752, false)
	}

	fun checkReady(){
		val ready = tabWidgets.all { it.isReady }
		tabWidgets.forEach { it.shouldShowDone = ready }
		if(ready && !isReady) {
			addDrawableChild(doneButton)
			isReady = true
		}
		else if(isReady && !ready) {
			remove(doneButton)
			isReady = false
		}
	}

	companion object {
		val BACKGROUND = Identifier.of(MTT.MOD_ID, "textures/gui/character_maker/background.png")
	}
}