package com.pineypiney.mtt.client.render.entity

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.render.MTTRenderers
import com.pineypiney.mtt.client.render.entity.model.DNDPlayerEntityModel
import com.pineypiney.mtt.client.render.entity.state.DNDPlayerEntityRenderState
import com.pineypiney.mtt.entity.TestEntity
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.util.Identifier

class TestEntityRenderer(ctx: EntityRendererFactory.Context) :
	LivingEntityRenderer<TestEntity, DNDPlayerEntityRenderState, DNDPlayerEntityModel>(
		ctx,
		DNDPlayerEntityModel(getModel(ctx)), .5f
	) {

	override fun getTexture(state: DNDPlayerEntityRenderState?): Identifier? {
		return Identifier.of(MTT.MOD_ID, "textures/entity/human/default/default.png")
	}

	override fun createRenderState(): DNDPlayerEntityRenderState {
		return DNDPlayerEntityRenderState()
	}

	companion object {
		fun getModel(ctx: EntityRendererFactory.Context): ModelPart{

			try {
				val layer = MTTRenderers.BIPED_MODELS["human/default"]?.second
					?: MTTRenderers.BIPED_MODELS.values.first().second
				return ctx.getPart(layer)
			} catch (e: IllegalArgumentException) {
				MTT.logger.error("Failed to create mode part")
				throw e
			}
		}
	}
}