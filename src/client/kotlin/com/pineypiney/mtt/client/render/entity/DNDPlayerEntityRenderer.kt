package com.pineypiney.mtt.client.render.entity

import com.pineypiney.mtt.client.render.entity.model.DNDPlayerEntityModel
import com.pineypiney.mtt.client.render.entity.state.DNDPlayerEntityRenderState
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.entity.DNDEntity
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.command.OrderedRenderCommandQueue
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.state.CameraRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm

class DNDPlayerEntityRenderer(ctx: EntityRendererFactory.Context) :
	DNDBipedEntityRenderer<DNDEntity, DNDPlayerEntityRenderState, DNDPlayerEntityModel>(
		ctx,
		::DNDPlayerEntityModel
	) {

	override fun createRenderState(): DNDPlayerEntityRenderState {
		return DNDPlayerEntityRenderState()
	}

	override fun render(
		state: DNDPlayerEntityRenderState,
		matrices: MatrixStack,
		queue: OrderedRenderCommandQueue,
		cameraState: CameraRenderState
	) {
		super.render(state, matrices, queue, cameraState)
	}

	fun renderArm(matrices: MatrixStack, queue: OrderedRenderCommandQueue, light: Int, arm: Arm, character: Character) {
		val (model, texture) = getModel(character) ?: return
		val armModel = when(arm) { Arm.LEFT -> model.leftArm; Arm.RIGHT -> model.rightArm }
		armModel.resetTransform()
		armModel.visible = true
		model.leftArm.roll = -0.1f
		model.rightArm.roll = 0.1f
		armModel.originY += 24f
		queue.submitModelPart(
			armModel,
			matrices,
			RenderLayers.entityTranslucent(texture),
			light,
			OverlayTexture.DEFAULT_UV,
			null
		)
	}
}