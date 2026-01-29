package com.pineypiney.mtt.render.entity

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.entity.DNDPlayerEntity
import com.pineypiney.mtt.render.entity.model.DNDPlayerEntityModel
import com.pineypiney.mtt.render.entity.state.DNDPlayerEntityRenderState
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm

class DNDPlayerEntityRenderer(ctx: EntityRendererFactory.Context): DNDBipedEntityRenderer<DNDPlayerEntity, DNDPlayerEntityRenderState, DNDPlayerEntityModel>(ctx, ::DNDPlayerEntityModel) {

	override fun createRenderState(): DNDPlayerEntityRenderState {
		return DNDPlayerEntityRenderState()
	}

	override fun render(
		state: DNDPlayerEntityRenderState,
		matrices: MatrixStack,
		vertexConsumers: VertexConsumerProvider,
		light: Int
	) {
		super.render(state, matrices, vertexConsumers, light)
	}

	fun renderArm(matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, arm: Arm, character: Character){
		val (model, texture) = getModel(character) ?: return
		val armModel = when(arm) { Arm.LEFT -> model.leftArm; Arm.RIGHT -> model.rightArm }
		armModel.resetTransform()
		armModel.visible = true
		model.leftArm.roll = -0.1f
		model.rightArm.roll = 0.1f
		val originY = model.leftArm.originY
		model.leftArm.originY = originY + 24f
		model.rightArm.originY = originY + 24f
		armModel.render(
			matrices,
			vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture)),
			light,
			OverlayTexture.DEFAULT_UV
		)
		model.leftArm.originY = originY
		model.rightArm.originY = originY
	}
}