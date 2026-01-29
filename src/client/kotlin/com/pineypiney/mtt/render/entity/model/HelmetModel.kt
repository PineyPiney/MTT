package com.pineypiney.mtt.render.entity.model

import com.pineypiney.mtt.render.entity.state.DNDBipedEntityRenderState
import net.minecraft.client.model.Model
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.math.MathHelper

class HelmetModel(root: ModelPart) : Model(root, RenderLayer::getEntityCutoutNoCull) {
	val head = root.getChild("head")

	fun setAngles(state: DNDBipedEntityRenderState){
		head.yaw = state.relativeHeadYaw * MathHelper.RADIANS_PER_DEGREE
		head.pitch = state.pitch * MathHelper.RADIANS_PER_DEGREE
	}
}