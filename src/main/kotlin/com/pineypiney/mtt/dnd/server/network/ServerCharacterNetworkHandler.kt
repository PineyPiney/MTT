package com.pineypiney.mtt.dnd.server.network

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.network.payloads.c2s.CharacterMoveC2SPayload
import com.pineypiney.mtt.network.payloads.c2s.TeleportConfirmC2SPayload
import com.pineypiney.mtt.network.payloads.s2c.CharacterPositionLookS2CPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.AbstractBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityPosition
import net.minecraft.entity.MovementType
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.WorldView
import net.minecraft.world.rule.GameRules

class ServerCharacterNetworkHandler(val engine: ServerDNDEngine, val player: ServerPlayerEntity) {

	var character: Character? = null

	private var ticks = 0
	private var lastTickX = 0.0
	private var lastTickY = 0.0
	private var lastTickZ = 0.0
	private var updatedX = 0.0
	private var updatedY = 0.0
	private var updatedZ = 0.0

	private var requestedTeleportPos: Vec3d? = null
	private var requestedTeleportId = 0
	private var lastTeleportCheckTicks = 0
	private var floating = false
	private var floatingTicks = 0
	private var movePacketsCount = 0
	private var lastTickMovePacketsCount = 0
	private var movedThisTick = false

	private var dead = false
	private var remainingLoadingTicks = 0

	fun onTeleportConfirm(packet: TeleportConfirmC2SPayload) {
		if (packet.teleportID == this.requestedTeleportId) {
			if (this.requestedTeleportPos == null) {
				return
			}
			val character = character ?: return
			val entity = engine.getEntityOfCharacter(character.uuid) ?: return

			entity.updatePositionAndAngles(
				this.requestedTeleportPos!!.x,
				this.requestedTeleportPos!!.y,
				this.requestedTeleportPos!!.z,
				entity.yaw,
				entity.pitch
			)
			this.updatedX = this.requestedTeleportPos!!.x
			this.updatedY = this.requestedTeleportPos!!.y
			this.updatedZ = this.requestedTeleportPos!!.z
			this.player.onTeleportationDone()
			this.requestedTeleportPos = null
		}
	}

	fun onPlayerMove(payload: CharacterMoveC2SPayload) {
		val serverWorld: ServerWorld = player.entityWorld
		val character = character ?: return
		val entity = engine.getEntityOfCharacter(character.uuid) ?: return
		if (!player.notInAnyWorld) {
			if (this.ticks == 0) {
				this.syncWithCharacterPosition(entity)
			}

			if (this.canInteractWithGame()) {
				val f: Float = MathHelper.wrapDegrees(payload.getYaw(entity.yaw))
				val g: Float = MathHelper.wrapDegrees(payload.getPitch(entity.pitch))
				if (this.handlePendingTeleport(entity)) {
					entity.setAngles(f, g)
				} else {
					val d = MathHelper.clamp(payload.getX(entity.x), -3e7, 3e7)
					val e = MathHelper.clamp(payload.getY(entity.y), -2e7, 2e7)
					val h = MathHelper.clamp(payload.getZ(entity.z), -3e7, 3e7)
					if (entity.hasVehicle()) {
						entity.updatePositionAndAngles(
							entity.x,
							entity.y,
							entity.z,
							f,
							g
						)
						player.entityWorld.chunkManager.updatePosition(player)
					} else {
						val i: Double = entity.x
						val j: Double = entity.y
						val k: Double = entity.z
						var l: Double = d - this.lastTickX
						var m: Double = e - this.lastTickY
						var n: Double = h - this.lastTickZ
						val o: Double = entity.velocity.lengthSquared()
						var p = l * l + m * m + n * n

						if (serverWorld.tickManager.shouldTick()) {
							this.movePacketsCount++
							var q: Int = this.movePacketsCount - this.lastTickMovePacketsCount
							if (q > 5) {
								MTT.logger.debug("${entity.stringifiedName} is sending move packets too frequently ($g packets since last tick)")
								q = 1
							}

							if (this.shouldCheckMovement()) {
								val r = 100.0f
								if (p - o > r * q) {
									MTT.logger.warn("${entity.stringifiedName} moved too quickly! $l,$m,$n}")
									this.requestTeleport(entity, entity.x, entity.y, entity.z, entity.yaw, entity.pitch)
									return
								}
							}
						}

						val box: Box = entity.boundingBox
						l = d - this.updatedX
						m = e - this.updatedY
						n = h - this.updatedZ
						val bl2 = m > 0.0
						if (entity.isOnGround && !payload.onGround && bl2) {
							entity.jump()
						}

						val bl3: Boolean = entity.groundCollision
						entity.move(MovementType.PLAYER, Vec3d(l, m, n))
						l = d - entity.x
						m = e - entity.y
						if (m > -0.5 || m < 0.5) {
							m = 0.0
						}
						n = h - entity.z

						p = l * l + m * m + n * n
						var bl4 = false
						if (!player.isInTeleportationState && p > 0.0625 && !player.isSleeping && !player.isCreative && !player.isSpectator && !player.isInCurrentExplosionResetGraceTime) {
							bl4 = true
							MTT.logger.warn(
								"${player.stringifiedName} moved wrongly!",

								)
						}

						if (entity.noClip ||
							player.isSleeping ||
							(!bl4 || !serverWorld.isSpaceEmpty(entity, box)) &&
							!this.isEntityNotCollidingWithBlocks(serverWorld, entity, box, d, e, h)
						) {
							entity.updatePositionAndAngles(d, e, h, f, g)
							this.floating =
								m >= -0.03125 &&
										!bl3 &&
										!player.isSpectator &&
										!this.engine.server.isFlightEnabled &&
										//!player.abilities.allowFlying &&
										//!player.hasStatusEffect(StatusEffects.LEVITATION) &&
										this.isEntityOnAir(entity)
							player.entityWorld.chunkManager.updatePosition(player)
							val vec3d = Vec3d(
								entity.x - i,
								entity.y - j,
								entity.z - k
							)
							entity.setMovement(
								payload.onGround,
								payload.horizontalCollision,
								vec3d
							)
							entity.handleFall(vec3d.x, vec3d.y, vec3d.z, payload.onGround)
							this.handleMovement(entity, vec3d)
							if (bl2) {
								entity.onLanding()
							}

							if (payload.onGround ||
								entity.hasLandedInFluid() ||
								entity.isClimbing() ||
								player.isSpectator
							) {
								player.tryClearCurrentExplosion()
							}
							this.updatedX = entity.x
							this.updatedY = entity.y
							this.updatedZ = entity.z
						} else {
							this.requestTeleport(entity, i, j, k, f, g)
							entity.handleFall(
								entity.x - i,
								entity.y - j,
								entity.z - k,
								payload.onGround
							)
							entity.popQueuedCollisionCheck()
						}
					}
				}
			}
		}
	}

	private fun shouldCheckMovement(): Boolean {
		if (this.player.isInTeleportationState) {
			return false
		} else {
			val gameRules = this.player.entityWorld.gameRules
			return !gameRules.getValue(GameRules.PLAYER_MOVEMENT_CHECK)
		}
	}

	private fun isEntityOnAir(entity: Entity): Boolean {
		return entity.entityWorld
			.getStatesInBox(entity.boundingBox.expand(0.0625).stretch(0.0, -0.55, 0.0))
			.allMatch(AbstractBlock.AbstractBlockState::isAir)
	}

	private fun handlePendingTeleport(entity: DNDEntity): Boolean {
		if (this.requestedTeleportPos != null) {
			if (this.ticks - this.lastTeleportCheckTicks > 20) {
				this.lastTeleportCheckTicks = this.ticks
				this.requestTeleport(
					entity,
					this.requestedTeleportPos!!.x,
					this.requestedTeleportPos!!.y,
					this.requestedTeleportPos!!.z,
					entity.yaw,
					entity.pitch
				)
			}

			return true
		} else {
			this.lastTeleportCheckTicks = this.ticks
			return false
		}
	}

	private fun isEntityNotCollidingWithBlocks(
		world: WorldView,
		entity: Entity,
		box: Box,
		newX: Double,
		newY: Double,
		newZ: Double
	): Boolean {
		val box2 = entity.boundingBox.offset(newX - entity.x, newY - entity.y, newZ - entity.z)
		val iterable = world.getCollisions(entity, box2.contract(1.0E-5), box.horizontalCenter)
		val voxelShape = VoxelShapes.cuboid(box.contract(1.0E-5))

		for (voxelShape2 in iterable) {
			if (!VoxelShapes.matchesAnywhere(voxelShape2, voxelShape, BooleanBiFunction.AND)) {
				return true
			}
		}

		return false
	}

	fun requestTeleport(entity: DNDEntity, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
		this.requestTeleport(entity, EntityPosition(Vec3d(x, y, z), Vec3d.ZERO, yaw, pitch), mutableSetOf())
	}

	fun requestTeleport(entity: DNDEntity, pos: EntityPosition, flags: MutableSet<PositionFlag>) {
		this.lastTeleportCheckTicks = this.ticks
		if (++this.requestedTeleportId == Int.MAX_VALUE) {
			this.requestedTeleportId = 0
		}

		entity.setPosition(pos, flags)
		this.requestedTeleportPos = entity.entityPos
		ServerPlayNetworking.send(player, CharacterPositionLookS2CPayload(requestedTeleportId, pos, flags))
	}

	fun syncWithCharacterPosition(entity: DNDEntity) {
		this.lastTickX = entity.x
		this.lastTickY = entity.y
		this.lastTickZ = entity.z
		this.updatedX = entity.x
		this.updatedY = entity.y
		this.updatedZ = entity.z
	}

	private fun handleMovement(entity: DNDEntity, movement: Vec3d) {
		if (movement.lengthSquared() > 1.0E-5f) {
			this.player.updateLastActionTime()
		}

		entity.setMovement(movement)
		this.movedThisTick = true
	}

	fun canInteractWithGame(): Boolean {
		return !this.dead && this.remainingLoadingTicks <= 0
	}

	fun tickLoading() {
		if (this.remainingLoadingTicks > 0) {
			this.remainingLoadingTicks--
		}
	}

	private fun markLoaded() {
		this.remainingLoadingTicks = 0
	}

	/**
	 * {@return marks the player as dead}
	 *
	 *
	 * This is not the correct method for killing the player. Use methods in
	 * [ServerPlayerEntity], like [ServerPlayerEntity.kill].
	 */
	fun markAsDead() {
		this.dead = true
	}

	private fun markRespawned() {
		this.dead = false
		this.remainingLoadingTicks = 60
	}
}