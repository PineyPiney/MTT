package com.pineypiney.mtt.render.entity

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.entity.TestEntity
import com.pineypiney.mtt.render.MTTRenderers
import com.pineypiney.mtt.render.entity.model.DNDPlayerEntityModel
import com.pineypiney.mtt.render.entity.state.DNDPlayerEntityRenderState
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.util.Identifier

class TestEntityRenderer(ctx: EntityRendererFactory.Context) : LivingEntityRenderer<TestEntity, DNDPlayerEntityRenderState, DNDPlayerEntityModel>(ctx, DNDPlayerEntityModel(ctx.getPart(MTTRenderers.BIPED_MODELS["default"])), .5f) {

	override fun getTexture(state: DNDPlayerEntityRenderState?): Identifier? {
		return Identifier.of(MTT.MOD_ID, "textures/entity/human/default.png")
	}

	override fun createRenderState(): DNDPlayerEntityRenderState? {
		return DNDPlayerEntityRenderState()
	}
}