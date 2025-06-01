package com.pineypiney.mtt.render.entity.model

import com.pineypiney.mtt.render.entity.state.DNDBipedEntityRenderState
import net.minecraft.client.model.Model
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer

class HelmetModel(root: ModelPart) : Model(root, RenderLayer::getEntityCutoutNoCull) {
	val head = root.getChild("head")

	fun setAngles(state: DNDBipedEntityRenderState){
		head.yaw = state.relativeHeadYaw
	}
}