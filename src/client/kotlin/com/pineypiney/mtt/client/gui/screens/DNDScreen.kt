package com.pineypiney.mtt.client.gui.screens

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.dnd.ClientDNDEngine
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.entity.DNDInventory
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.screen.DNDScreenHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.client.render.entity.state.LivingEntityRenderState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityPose
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan

class DNDScreen(handler: DNDScreenHandler, playerInventory: PlayerInventory, title: Text) : HandledScreen<DNDScreenHandler>(handler, playerInventory, title) {

	init {
		titleX = 139
		backgroundWidth = 326
		backgroundHeight = 150
	}

	private var scrolling = false
	private var scrollPosition = 0f
	private val searchBox = TextFieldWidget(textRenderer, x + 213, y + 6, 80, 9, Text.translatable("itemGroup.search"))
	private lateinit var engine: ClientDNDEngine

	override fun init() {
		super.init()
		engine = (client as DNDEngineHolder<*>).`mtt$getDNDEngine`() as ClientDNDEngine
		searchBox.setMaxLength(50)
		searchBox.setDrawsBackground(false)
		searchBox.isVisible = false
		searchBox.setEditableColor(0xffffff)
		addSelectableChild(searchBox)

		handler.scrollItems(0f)
	}

	fun hasScrollbar(): Boolean{
		val character = engine.getCharacterFromPlayer(client?.player?.uuid ?: return false) ?: return false
		return character.inventory.size() > DNDInventory.EQUIPMENT_SIZE + 63
	}

	override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) {
		context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, -12566464, false)
	}

	override fun drawBackground(context: DrawContext, deltaTicks: Float, mouseX: Int, mouseY: Int) {
		context.drawTexture(
			RenderPipelines.GUI_TEXTURED,
			INVENTORY,
			x + 131,
			y,
			0f,
			0f,
			195,
			backgroundHeight,
			256,
			256
		)
		context.drawTexture(RenderPipelines.GUI_TEXTURED, EQUIPMENT, x, y + 11, 0f, 0f, 128, 128, 128, 128)

		searchBox.render(context, mouseX, mouseY, deltaTicks)

		val identifier = if (this.hasScrollbar()) SCROLLER_TEXTURE else SCROLLER_DISABLED_TEXTURE
		context.drawGuiTexture(
			RenderPipelines.GUI_TEXTURED,
			identifier,
			x + 306,
			y + 18 + (95f * this.scrollPosition).toInt(),
			12,
			15
		)

		val player = engine.getEntityOfCharacter(handler.character.uuid) ?: return
		drawEntity(context, x + 44, y + 19, x + 84, y + 107, 36, 0f, mouseX.toFloat(), mouseY.toFloat(), player)
	}

	override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		super.render(context, mouseX, mouseY, deltaTicks)
		drawMouseoverTooltip(context, mouseX, mouseY)
	}

	override fun mouseDragged(click: Click, offsetX: Double, offsetY: Double): Boolean {
		if (this.scrolling) {
			val i = this.y + 18
			val j = i + 112
			this.scrollPosition = (click.y.toFloat() - i.toFloat() - 7.5f) / ((j - i).toFloat() - 15.0f)
			this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f)
			(this.handler as DNDScreenHandler).scrollItems(this.scrollPosition)
			return true
		} else {
			return super.mouseDragged(click, offsetX, offsetY)
		}
	}

	companion object {
		val INVENTORY: Identifier = Identifier.of(MTT.MOD_ID, "textures/gui/container/dnd_inventory/item_search.png")
		val EQUIPMENT: Identifier = Identifier.of(MTT.MOD_ID, "textures/gui/container/dnd_inventory/equipment.png")

		private val SCROLLER_TEXTURE: Identifier = Identifier.ofVanilla("container/creative_inventory/scroller")
		private val SCROLLER_DISABLED_TEXTURE: Identifier = Identifier.ofVanilla("container/creative_inventory/scroller_disabled")

		private val NAME = Text.translatable("mtt.screen.dnd_inventory")

		/**
		 * Direct copy of [net.minecraft.client.gui.screen.ingame.InventoryScreen.drawEntity]
		 */
		fun drawEntity(context: DrawContext, x1: Int, y1: Int, x2: Int, y2: Int, size: Int, scale: Float, mouseX: Float, mouseY: Float, entity: DNDEntity) {
			val f = (x1 + x2) * .5f
			val g = (y1 + y2) * .5f
			val h = atan(((f - mouseX) * .025f))
			val i = atan(((g - mouseY) * .025f))
			val quaternion = Quaternionf().rotateZ(Math.PI.toFloat())
			val quaternion2 = Quaternionf().rotateX(i * 20f * MathHelper.RADIANS_PER_DEGREE)
			quaternion.mul(quaternion2)

//			val entity = MinecraftClient.getInstance().world?.getEntitiesByType(EntityType.HORSE, Box(entity.entityPos.subtract(10.0), entity.entityPos.add(10.0))){ true }?.firstOrNull() ?: return
			val entityRenderState = getEntityState(entity)
			if (entityRenderState is LivingEntityRenderState) {
				entityRenderState.bodyYaw = 180.0f + h * 20.0f
				entityRenderState.relativeHeadYaw = h * 20.0f
				if (entityRenderState.pose != EntityPose.GLIDING) {
					entityRenderState.pitch = -i * 20.0f
				} else {
					entityRenderState.pitch = 0.0f
				}

				entityRenderState.width /= entityRenderState.baseScale
				entityRenderState.height /= entityRenderState.baseScale
				entityRenderState.baseScale = 1.0f

				entityRenderState.invisibleToPlayer = false
				entityRenderState.displayName = null
			}
			val vector3f = Vector3f(0.0f, entity.height * .5f + scale, 0.0f)
			context.addEntity(entityRenderState, size.toFloat(), vector3f, quaternion, quaternion2, x1, y1, x2, y2)
		}

		private fun getEntityState(entity: Entity): EntityRenderState {
			val entityRenderManager = MinecraftClient.getInstance().entityRenderDispatcher
			val entityRenderer = entityRenderManager.getRenderer(entity)
			val entityRenderState: EntityRenderState = entityRenderer.getAndUpdateRenderState(entity, 1.0f)
			entityRenderState.light = 15728880
			entityRenderState.shadowPieces.clear()
			entityRenderState.outlineColor = 0
			return entityRenderState
		}
	}
}