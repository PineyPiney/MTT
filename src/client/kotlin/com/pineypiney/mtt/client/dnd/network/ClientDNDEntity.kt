package com.pineypiney.mtt.client.dnd.network

import com.pineypiney.mtt.client.dnd.DNDClientEngine
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.util.getEngine
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.ClientPlayerLikeState
import net.minecraft.entity.Entity
import net.minecraft.entity.MovementType
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.min
import kotlin.math.sqrt

class ClientDNDEntity(world: World) : DNDEntity(world) {

	val state = ClientPlayerLikeState()

	override fun tick() {
		state.tick(entityPos, velocity)
		super.tick()
	}

	override fun tickMovement() {
		if (clientIsControlling()) {
			val player = MinecraftClient.getInstance().player ?: return


//			val bl: Boolean = player.input.playerInput.jump()
			val bl2: Boolean = player.input.playerInput.sneak()
			val bl3: Boolean = player.input.hasForwardMovement()

			if (bl2 || this.hasVehicle() || player.input.playerInput.backward()) {
				player.ticksLeftToDoubleTapSprint = 0
			}

			if (canStartSprinting(player)) {
				if (!bl3) {
					if (player.ticksLeftToDoubleTapSprint > 0) {
						this.isSprinting = true
					} else {
						player.ticksLeftToDoubleTapSprint =
							MinecraftClient.getInstance().options.sprintWindow.getValue()
					}
				}

				if (player.input.playerInput.sprint()) {
					this.isSprinting = true
				}
			}

			if (this.isSprinting) {
				if (this.isSwimming) {
					if (this.shouldStopSwimSprinting(player)) {
						this.isSprinting = false
					}
				} else if (this.shouldStopSprinting(player)) {
					this.isSprinting = false
				}
			}

		}
		super.tickMovement()
		val f = if (!isOnGround) 0f
		else min(velocity.horizontalLength().toFloat(), .1f)
		state.tickMovement(f)
	}

	override fun move(type: MovementType, movement: Vec3d) {
		if (DNDClientEngine.getClientCharacter() != character) return
		val oldX = x
		val oldZ = z
		super.move(type, movement)
		val dx = x - oldX
		val dz = z - oldZ
		state.addDistanceMoved(sqrt(dx * dx + dz * dz).toFloat() * .6f)
	}

	private fun canStartSprinting(player: ClientPlayerEntity): Boolean {
		return !this.isSprinting && player.input.hasForwardMovement() && canSprint()
//				&& !this.isBlockedFromSprinting() && (!this.isGliding() || this.isSubmergedInWater())
//				&& (!this.shouldSlowDown() || this.isSubmergedInWater())
	}

	private fun canSprint(): Boolean {
		val vehicle = vehicle
		return (if (vehicle != null) this.canVehicleSprint(vehicle) else true)
	}

	private fun canVehicleSprint(vehicle: Entity): Boolean {
		return vehicle.canSprintAsVehicle() && vehicle.isLogicalSideForUpdatingMovement
	}

	private fun shouldStopSprinting(player: ClientPlayerEntity): Boolean {
		return !this.canSprint() || !player.input.hasForwardMovement() || this.horizontalCollision && !this.collidedSoftly
	}

	private fun shouldStopSwimSprinting(player: ClientPlayerEntity): Boolean {
		return !this.canSprint() || !this.isTouchingWater || !player.input.hasForwardMovement() && !this.isOnGround && !player.input.playerInput.sneak()
	}

	override fun isControlledByMainPlayer(): Boolean {
		val engine = entityWorld.getEngine()
		return engine.running && engine.getCharacterFromPlayer(
			MinecraftClient.getInstance().player?.uuid ?: return false
		) == character
	}

	fun getFovMultiplier(firstPerson: Boolean, fovEffectScale: Float): Float {
		var f = 1.0f

		val h = movementSpeed * 10f
		f *= (h + 1.0f) * .5f

//		if (this.isUsingItem()) {
//			if (this.getActiveItem().isOf(Items.BOW)) {
//				val h = min(this.getItemUseTime() / 20.0f, 1.0f)
//				f *= 1.0f - MathHelper.square(h) * 0.15f
//			} else if (firstPerson && this.isUsingSpyglass()) {
//				return 0.1f
//			}
//		}

		return MathHelper.lerp(fovEffectScale, 1.0f, f)
	}

	fun clientIsControlling(): Boolean {
		return DNDClientEngine.getClientCharacter() == character
	}
}