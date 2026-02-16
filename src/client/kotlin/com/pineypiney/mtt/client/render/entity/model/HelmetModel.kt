package com.pineypiney.mtt.client.render.entity.model

import com.pineypiney.mtt.client.render.entity.state.DNDBipedEntityRenderState
import net.minecraft.client.model.Model
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayers
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper

class HelmetModel<S : DNDBipedEntityRenderState>(
	root: ModelPart,
	factory: (Identifier) -> RenderLayer = RenderLayers::entityCutoutNoCull
) : Model<S>(root, factory) {
	val head = root.getChild("head")

	override fun setAngles(state: S) {
		head.yaw = state.relativeHeadYaw * MathHelper.RADIANS_PER_DEGREE
		head.pitch = state.pitch * MathHelper.RADIANS_PER_DEGREE
	}
}