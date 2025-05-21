package com.pineypiney.mtt.render.entity.model

import com.pineypiney.mtt.render.entity.state.DNDPlayerEntityRenderState
import net.minecraft.client.model.*
import net.minecraft.client.render.entity.model.EntityModelPartNames

class DNDPlayerEntityModel(root: ModelPart) : DNDBipedEntityModel<DNDPlayerEntityRenderState>(root) {

	companion object {
		fun getTexturedModelData(): TexturedModelData{
			val data = ModelData()
			val root = data.root
			val head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 0).cuboid(-4f, 0f, -4f, 8f, 8f, 8f), ModelTransform.origin(0f, 24f, 0f))
			//head.addChild(EntityModelPartNames.HAT)
			root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(16, 16).cuboid(-4f, 12f, -2f, 8f, 12f, 4f), ModelTransform.NONE)
			root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(0, 16).cuboid(-2f, -12f, -2f, 4f, 12f, 4f), ModelTransform.origin(2f, 12f, 0f))
			root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(16, 48).cuboid(-2f, -12f, -2f, 4f, 12f, 4f), ModelTransform.origin(-2f, 12f, 0f))
			root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create().uv(40, 16).cuboid(-1f, -10f, -2f, 4f, 12f, 4f), ModelTransform.origin(5f, 22f, 0f))
			root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create().uv(32, 48).cuboid(-3f, -10f, -2f, 4f, 12f, 4f), ModelTransform.origin(-5f, 22f, 0f))

			return TexturedModelData.of(data, 64, 64)
		}
	}
}