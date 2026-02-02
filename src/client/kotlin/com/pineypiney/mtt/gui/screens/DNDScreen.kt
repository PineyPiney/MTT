package com.pineypiney.mtt.gui.screens

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DNDClientEngine
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.entity.DNDInventory
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.screen.DNDScreenHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.MathHelper
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan
import kotlin.math.sin

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
	private lateinit var engine: DNDClientEngine

	override fun init() {
		super.init()
		engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`() as DNDClientEngine
		searchBox.setMaxLength(50)
		searchBox.setDrawsBackground(false)
		searchBox.isVisible = false
		searchBox.setEditableColor(0xffffff)
		addSelectableChild(searchBox)

		handler.scrollItems(0f)
	}

	override fun charTyped(chr: Char, modifiers: Int): Boolean {
		return super.charTyped(chr, modifiers)
	}

	fun hasScrollbar(): Boolean{
		val character = engine.getPlayerCharacter(client?.player?.uuid ?: return false) ?: return false
		return character.inventory.size() > DNDInventory.EQUIPMENT_SIZE + 63
	}

	override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
		context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false)
	}

	override fun drawBackground(context: DrawContext, deltaTicks: Float, mouseX: Int, mouseY: Int) {
		context.drawTexture(RenderLayer::getGuiTextured, INVENTORY, x + 131, y, 0f, 0f, 195, backgroundHeight, 256, 256)
		context.drawTexture(RenderLayer::getGuiTextured, EQUIPMENT, x, y + 11, 0f, 0f, 128, 128, 128, 128)

		searchBox.render(context, mouseX, mouseY, deltaTicks)

		val identifier = if (this.hasScrollbar()) SCROLLER_TEXTURE else SCROLLER_DISABLED_TEXTURE
		context.drawGuiTexture(RenderLayer::getGuiTextured, identifier, x + 306, y + 18 + (95f * this.scrollPosition).toInt(), 12, 15)

		val player = engine.getPlayerEntity(client?.player?.uuid ?: return) ?: return
		drawEntity(context, x + 44, y + 19, x + 84, y + 107, 36, 0f, mouseX.toFloat(), mouseY.toFloat(), player)
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

		fun drawEntity(context: DrawContext, x1: Int, y1: Int, x2: Int, y2: Int, size: Int, f: Float, mouseX: Float, mouseY: Float, entity: DNDEntity) {
			val g = (x1 + x2) * .5f
			val h = (y1 + y2) * .5f
			context.enableScissor(x1, y1, x2, y2)
			val i = atan(((g - mouseX) * .025f))
			val j = atan(((h - mouseY) * .025f))
			val quaternion = Quaternionf().rotateZ(Math.PI.toFloat())
			val quaternion2 = Quaternionf().rotateX(j * 20f * MathHelper.RADIANS_PER_DEGREE)
			quaternion.mul(quaternion2)
			val k = entity.bodyYaw
			val l = entity.yaw
			val m = entity.pitch
			val n = entity.lastHeadYaw
			val o = entity.headYaw
			entity.bodyYaw = 180f + i * 20f
			entity.yaw = 180f + i * 40f
			entity.pitch = -j * 20f
			entity.headYaw = entity.yaw
			entity.lastHeadYaw = entity.yaw
			val vector3f = Vector3f(0.0f, entity.height * .5f + f, 0.0f)
			drawEntity(context, g, h, size.toFloat(), vector3f, quaternion, quaternion2, entity)
			entity.bodyYaw = k
			entity.yaw = l
			entity.pitch = m
			entity.lastHeadYaw = n
			entity.headYaw = o
			context.disableScissor()
		}

		fun drawEntity(context: DrawContext, x: Float, y: Float, size: Float, vector3f: Vector3f, quaternionf: Quaternionf, quaternionf2: Quaternionf?, entity: DNDEntity) {
			context.matrices.push()
			context.matrices.translate(x.toDouble(), y.toDouble(), 50.0)
			context.matrices.scale(size, size, -size)
			context.matrices.translate(vector3f.x, vector3f.y, vector3f.z)
			context.matrices.multiply(quaternionf)
			context.draw()

			DiffuseLighting.enableGuiShaderLighting()
			val entityRenderDispatcher = MinecraftClient.getInstance().entityRenderDispatcher
			if (quaternionf2 != null) {
				entityRenderDispatcher.rotation = quaternionf2.conjugate(Quaternionf()).rotateY(Math.PI.toFloat())
			}

			entityRenderDispatcher.setRenderShadows(false)
			val t = (Util.getMeasuringTimeMs() % 100000) * 3e-3
			context.draw { vertexConsumers: VertexConsumerProvider ->
				entityRenderDispatcher.render(
					entity,
					0.0,
					.03 * sin(t),
					0.0,
					1.0f,
					context.matrices,
					vertexConsumers,
					15728880
				)
			}
			context.draw()
			entityRenderDispatcher.setRenderShadows(true)
			context.matrices.pop()
			DiffuseLighting.enableGuiDepthLighting()
		}
	}
}