package com.pineypiney.mtt.render.entity.model

import com.pineypiney.mtt.render.entity.state.DNDBipedEntityRenderState
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.EntityModelPartNames
import kotlin.math.sin

open class DNDBipedEntityModel<S: DNDBipedEntityRenderState>(root: ModelPart): EntityModel<S>(root) {
	val body: ModelPart = root.getChild(EntityModelPartNames.BODY)
	val head: ModelPart = root.getChild(EntityModelPartNames.HEAD)
	val leftLeg: ModelPart = root.getChild(EntityModelPartNames.LEFT_LEG)
	val rightLeg: ModelPart = root.getChild(EntityModelPartNames.RIGHT_LEG)
	val leftArm: ModelPart = root.getChild(EntityModelPartNames.LEFT_ARM)
	val rightArm: ModelPart = root.getChild(EntityModelPartNames.RIGHT_ARM)

	override fun setAngles(state: S) {
		super.setAngles(state)
		leftLeg.pitch = sin(state.limbSwingAnimationProgress * 0.666f) * state.limbSwingAmplitude
		rightLeg.pitch = -sin(state.limbSwingAnimationProgress * 0.666f) * state.limbSwingAmplitude
		leftArm.pitch = -sin(state.limbSwingAnimationProgress * 0.666f) * state.limbSwingAmplitude
		rightArm.pitch = sin(state.limbSwingAnimationProgress * 0.666f) * state.limbSwingAmplitude
		head.yaw = state.relativeHeadYaw
	}
}