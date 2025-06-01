package com.pineypiney.mtt.render.entity

import com.pineypiney.mtt.entity.DNDPlayerEntity
import com.pineypiney.mtt.render.entity.model.DNDPlayerEntityModel
import com.pineypiney.mtt.render.entity.state.DNDPlayerEntityRenderState
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack

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
}