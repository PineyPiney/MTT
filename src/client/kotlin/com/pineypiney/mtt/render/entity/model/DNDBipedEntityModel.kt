package com.pineypiney.mtt.render.entity.model

import com.pineypiney.mtt.render.entity.state.DNDBipedEntityRenderState
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.entity.model.ArmPosing
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.EntityModelPartNames
import net.minecraft.util.Arm
import net.minecraft.util.Hand
import net.minecraft.util.math.MathHelper
import kotlin.math.cos

open class DNDBipedEntityModel<S: DNDBipedEntityRenderState>(root: ModelPart): EntityModel<S>(root) {
	val body: ModelPart = root.getChild(EntityModelPartNames.BODY)
	val head: ModelPart = root.getChild(EntityModelPartNames.HEAD)
	val leftLeg: ModelPart = root.getChild(EntityModelPartNames.LEFT_LEG)
	val rightLeg: ModelPart = root.getChild(EntityModelPartNames.RIGHT_LEG)
	val leftArm: ModelPart = root.getChild(EntityModelPartNames.LEFT_ARM)
	val rightArm: ModelPart = root.getChild(EntityModelPartNames.RIGHT_ARM)

	override fun setAngles(bipedEntityRenderState: S) {
		super.setAngles(bipedEntityRenderState)

		val armPose = bipedEntityRenderState.leftArmPose
		val armPose2 = bipedEntityRenderState.rightArmPose

		this.head.pitch = bipedEntityRenderState.pitch * MathHelper.RADIANS_PER_DEGREE
		this.head.yaw = bipedEntityRenderState.relativeHeadYaw * MathHelper.RADIANS_PER_DEGREE

		val g = bipedEntityRenderState.limbSwingAnimationProgress
		val h = bipedEntityRenderState.limbSwingAmplitude
		this.rightArm.pitch = cos(g * 0.6662f + Math.PI.toFloat()) * 2.0f * h * 0.5f / bipedEntityRenderState.limbAmplitudeInverse
		this.leftArm.pitch = cos(g * 0.6662f) * 2.0f * h * 0.5f / bipedEntityRenderState.limbAmplitudeInverse
		this.rightLeg.pitch = cos(g * 0.6662f) * 1.4f * h / bipedEntityRenderState.limbAmplitudeInverse
		this.leftLeg.pitch = cos(g * 0.6662f + Math.PI.toFloat()) * 1.4f * h / bipedEntityRenderState.limbAmplitudeInverse
		this.rightLeg.yaw = 0.005f
		this.leftLeg.yaw = -0.005f
		this.rightLeg.roll = 0.005f
		this.leftLeg.roll = -0.005f


		val bl2 = bipedEntityRenderState.mainArm == Arm.RIGHT
		if (bipedEntityRenderState.isUsingItem) {
			val bl3 = bipedEntityRenderState.activeHand == Hand.MAIN_HAND
			if (bl3 == bl2) {
				this.positionRightArm(bipedEntityRenderState, armPose2)
			} else {
				this.positionLeftArm(bipedEntityRenderState, armPose)
			}
		} else {
			val bl3: Boolean = if (bl2) armPose.isTwoHanded() else armPose2.isTwoHanded()
			if (bl2 != bl3) {
				this.positionLeftArm(bipedEntityRenderState, armPose)
				this.positionRightArm(bipedEntityRenderState, armPose2)
			} else {
				this.positionRightArm(bipedEntityRenderState, armPose2)
				this.positionLeftArm(bipedEntityRenderState, armPose)
			}
		}
	}

	private fun positionRightArm(state: S, armPose: ArmPose) {
		when (armPose) {
			ArmPose.EMPTY -> this.rightArm.yaw = 0.0f
			ArmPose.ITEM -> {
				this.rightArm.pitch = this.rightArm.pitch * 0.5f - (Math.PI / 10).toFloat()
				this.rightArm.yaw = 0.0f
			}

			ArmPose.BLOCK -> this.positionBlockingArm(this.rightArm, true)
			ArmPose.BOW_AND_ARROW -> {
				this.rightArm.yaw = -0.1f + this.head.yaw
				this.leftArm.yaw = 0.1f + this.head.yaw + 0.4f
				this.rightArm.pitch = (-Math.PI / 2).toFloat() + this.head.pitch
				this.leftArm.pitch = (-Math.PI / 2).toFloat() + this.head.pitch
			}

			ArmPose.THROW_SPEAR -> {
				this.rightArm.pitch = this.rightArm.pitch * 0.5f - Math.PI.toFloat()
				this.rightArm.yaw = 0.0f
			}

			ArmPose.CROSSBOW_CHARGE -> ArmPosing.charge(
				this.rightArm,
				this.leftArm,
				state.crossbowPullTime,
				state.itemUseTime,
				true
			)

			ArmPose.CROSSBOW_HOLD -> ArmPosing.hold(this.rightArm, this.leftArm, this.head, true)
			ArmPose.SPYGLASS -> {
				this.rightArm.pitch = MathHelper.clamp(
					this.head.pitch - 1.9198622f - (if (state.isInSneakingPose) (Math.PI / 12).toFloat() else 0.0f),
					-2.4f,
					3.3f
				)
				this.rightArm.yaw = this.head.yaw - (Math.PI / 12).toFloat()
			}

			ArmPose.TOOT_HORN -> {
				this.rightArm.pitch = MathHelper.clamp(this.head.pitch, -1.2f, 1.2f) - 1.4835298f
				this.rightArm.yaw = this.head.yaw - (Math.PI / 6).toFloat()
			}

			ArmPose.BRUSH -> {
				this.rightArm.pitch = this.rightArm.pitch * 0.5f - (Math.PI / 5).toFloat()
				this.rightArm.yaw = 0.0f
			}
		}
	}

	private fun positionLeftArm(state: S, armPose: ArmPose) {
		when (armPose) {
			ArmPose.EMPTY -> this.leftArm.yaw = 0.0f
			ArmPose.ITEM -> {
				this.leftArm.pitch = this.leftArm.pitch * 0.5f - (Math.PI / 10).toFloat()
				this.leftArm.yaw = 0.0f
			}

			ArmPose.BLOCK -> this.positionBlockingArm(this.leftArm, false)
			ArmPose.BOW_AND_ARROW -> {
				this.rightArm.yaw = -0.1f + this.head.yaw - 0.4f
				this.leftArm.yaw = 0.1f + this.head.yaw
				this.rightArm.pitch = (-Math.PI / 2).toFloat() + this.head.pitch
				this.leftArm.pitch = (-Math.PI / 2).toFloat() + this.head.pitch
			}

			ArmPose.THROW_SPEAR -> {
				this.leftArm.pitch = this.leftArm.pitch * 0.5f - Math.PI.toFloat()
				this.leftArm.yaw = 0.0f
			}

			ArmPose.CROSSBOW_CHARGE -> ArmPosing.charge(
				this.rightArm,
				this.leftArm,
				state.crossbowPullTime,
				state.itemUseTime,
				false
			)

			ArmPose.CROSSBOW_HOLD -> ArmPosing.hold(this.rightArm, this.leftArm, this.head, false)
			ArmPose.SPYGLASS -> {
				this.leftArm.pitch = MathHelper.clamp(
					this.head.pitch - 1.9198622f - (if (state.isInSneakingPose) (Math.PI / 12).toFloat() else 0.0f),
					-2.4f,
					3.3f
				)
				this.leftArm.yaw = this.head.yaw + (Math.PI / 12).toFloat()
			}

			ArmPose.TOOT_HORN -> {
				this.leftArm.pitch = MathHelper.clamp(this.head.pitch, -1.2f, 1.2f) - 1.4835298f
				this.leftArm.yaw = this.head.yaw + (Math.PI / 6).toFloat()
			}

			ArmPose.BRUSH -> {
				this.leftArm.pitch = this.leftArm.pitch * 0.5f - (Math.PI / 5).toFloat()
				this.leftArm.yaw = 0.0f
			}
		}
	}

	private fun positionBlockingArm(arm: ModelPart, rightArm: Boolean) {
		arm.pitch = arm.pitch * 0.5f - 0.9424779f + MathHelper.clamp(
			this.head.pitch,
			(-Math.PI * 4.0 / 9.0).toFloat(),
			0.43633232f
		)
		arm.yaw = (if (rightArm) -30.0f else 30.0f) * (Math.PI / 180.0).toFloat() + MathHelper.clamp(
			this.head.yaw,
			(-Math.PI / 6).toFloat(),
			(Math.PI / 6).toFloat()
		)
	}
}