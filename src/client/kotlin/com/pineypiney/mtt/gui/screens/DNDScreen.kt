package com.pineypiney.mtt.gui.screens

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.entity.DNDInventory
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.screen.DNDScreenHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import java.util.function.Function

class DNDScreen(handler: DNDScreenHandler, playerInventory: PlayerInventory, title: Text) : HandledScreen<DNDScreenHandler>(handler, playerInventory, title) {

	init {
		titleX = 139
		backgroundWidth = 326
		backgroundHeight = 150
	}

	constructor(handler: DNDScreenHandler, playerInventory: PlayerInventory): this(handler, playerInventory, Text.translatable("mtt.screen.dnd_inventory"))

	private var scrolling = false
	private var scrollPosition = 0f
	private val searchBox = TextFieldWidget(textRenderer, x + 213, y + 6, 80, 9, Text.translatable("itemGroup.search"))

	override fun init() {
		super.init()
		searchBox.setMaxLength(50)
		searchBox.setDrawsBackground(false)
		searchBox.isVisible = false
		searchBox.setEditableColor(0xffffff)
		addSelectableChild(searchBox)

		handler.scrollItems(0f)
	}

	override fun resize(client: MinecraftClient?, width: Int, height: Int) {
		super.resize(client, width, height)
	}

	override fun charTyped(chr: Char, modifiers: Int): Boolean {
		return super.charTyped(chr, modifiers)
	}

	fun hasScrollbar(): Boolean{
		val character = (client as DNDEngineHolder<*>).dndEngine.getPlayer(client?.player?.uuid ?: return false) ?: return false
		return character.inventory.size() > DNDInventory.EQUIPMENT_SIZE + 63
	}

	override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
		context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false)
	}

	override fun drawBackground(context: DrawContext?, deltaTicks: Float, mouseX: Int, mouseY: Int) {
		context?.drawTexture(RenderLayer::getGuiTextured, INVENTORY, x + 131, y, 0f, 0f, 195, backgroundHeight, 256, 256)
		context?.drawTexture(RenderLayer::getGuiTextured, EQUIPMENT, x, y + 11, 0f, 0f, 128, 128, 128, 128)

		searchBox.render(context, mouseX, mouseY, deltaTicks)

		val identifier = if (this.hasScrollbar()) SCROLLER_TEXTURE else SCROLLER_DISABLED_TEXTURE
		context!!.drawGuiTexture(Function { texture: Identifier? -> RenderLayer.getGuiTextured(texture) }, identifier, x + 306, y + 18 + (95f * this.scrollPosition).toInt(), 12, 15)

		val player = client?.player ?: return
		InventoryScreen.drawEntity(context, x + 44, y + 19, x + 84, y + 107, 36, 0f, mouseX.toFloat(), mouseY.toFloat(), player)
	}

	override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		super.render(context, mouseX, mouseY, deltaTicks)
		drawMouseoverTooltip(context, mouseX, mouseY)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (this.scrolling) {
			val i = this.y + 18
			val j = i + 112
			this.scrollPosition = (mouseY.toFloat() - i.toFloat() - 7.5f) / ((j - i).toFloat() - 15.0f)
			this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f)
			(this.handler as DNDScreenHandler).scrollItems(this.scrollPosition)
			return true
		} else {
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
		}
	}

	companion object {
		val INVENTORY: Identifier = Identifier.of(MTT.MOD_ID, "textures/gui/container/dnd_inventory/item_search.png")
		val EQUIPMENT: Identifier = Identifier.of(MTT.MOD_ID, "textures/gui/container/dnd_inventory/equipment.png")

		private val SCROLLER_TEXTURE: Identifier = Identifier.ofVanilla("container/creative_inventory/scroller")
		private val SCROLLER_DISABLED_TEXTURE: Identifier = Identifier.ofVanilla("container/creative_inventory/scroller_disabled")


		private val NAME = Text.translatable("mtt.screen.dnd_inventory")
	}
}