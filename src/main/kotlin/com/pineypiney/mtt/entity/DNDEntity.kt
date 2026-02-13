package com.pineypiney.mtt.entity

import com.google.common.annotations.VisibleForTesting
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.network.payloads.s2c.EntityDNDEquipmentUpdateS2CPayload
import com.pineypiney.mtt.screen.DNDScreenHandler
import com.pineypiney.mtt.util.getEngine
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.EntityTypeTags
import net.minecraft.registry.tag.FluidTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.storage.ReadView
import net.minecraft.storage.WriteView
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.profiler.Profilers
import net.minecraft.world.World
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class DNDEntity(world: World) : Entity(MTTEntities.DND_ENTITY, world) {

	var character: Character? = null

	var hipHeight: Float = .75f
	val limbAnimator = LimbAnimator()
	var entityBodyYaw: Float = 0f
	var lastBodyYaw: Float = 0f
	var entityHeadYaw: Float = 0f
	var lastHeadYaw: Float = 0f
	val lastEquippedStacks = Array(20){ ItemStack.EMPTY }

	private var lastBlockPos: BlockPos? = null
	private var climbingPos: BlockPos? = null

	val movementSpeed get() = if (isSprinting) .13f else .1f
	var sidewaysSpeed = 0f
	var upwardSpeed = 0f
	var forwardSpeed = 0f
	var jumping = false

	private var jumpingCooldown = 0
	private var noDrag = false
	private var headTrackingIncrements = 0
	private var serverHeadYaw = 0.0

	var name: String
		get() = customName?.string ?: "Unnamed Character"
		set(value) { customName = Text.literal(value) }

	private var movement = Vec3d.ZERO

	val screenHandler = DNDScreenHandler(1, character?.inventory ?: DNDInventory())

	init {
		isCustomNameVisible = true
	}

	constructor(world: World, character: Character) : this(world) {
		this.character = character
		this.name = character.name
		this.setPosition(character.pos)
		dataTracker.set(CHARACTER_UUID, character.uuid)
		calculateDimensions()
	}

	override fun initDataTracker(builder: DataTracker.Builder) {
		builder.add(CHARACTER_UUID, UUID.randomUUID())
		builder.add(CONTROLLING_PLAYER, Optional.empty())
	}

	override fun onDataTrackerUpdate(entries: List<DataTracker.SerializedEntry<*>?>?) {
		val engine = entityWorld.getEngine()
		character = engine.getCharacter(dataTracker.get(CHARACTER_UUID)) ?: character
		calculateDimensions()
		uuid
	}


	open fun isClimbing(): Boolean {
		val blockPos = this.blockPos
		val blockState = this.blockStateAtPos
		if (blockState.isIn(BlockTags.CLIMBABLE) || (blockState.block is TrapdoorBlock && this.canEnterTrapdoor(
				blockPos,
				blockState
			))
		) {
			this.climbingPos = blockPos
			return true
		} else return false
	}

	private fun canEnterTrapdoor(pos: BlockPos, state: BlockState): Boolean {
		if (state.get(TrapdoorBlock.OPEN) != true) {
			return false
		} else {
			val ladder = this.entityWorld.getBlockState(pos.down())
			return ladder.isOf(Blocks.LADDER) && ladder.get(LadderBlock.FACING) == state.get(
				TrapdoorBlock.FACING
			)
		}
	}


	open fun travel(movementInput: Vec3d) {
		if (this.isTravellingInFluid(this.entityWorld.getFluidState(this.blockPos))) {
			this.travelInFluid(movementInput)
		} else {
			this.travelMidAir(movementInput)
		}
	}

	protected fun isTravellingInFluid(state: FluidState?): Boolean {
		return (this.isTouchingWater || this.isInLava)
	}

	private fun travelMidAir(movementInput: Vec3d) {
		val blockPos = this.velocityAffectingPos
		val f =
			if (this.isOnGround) this.entityWorld.getBlockState(blockPos).block.getSlipperiness() else 1.0f
		val g = f * 0.91f
		val vec3d: Vec3d = this.applyMovementInput(movementInput, f)
		val d = if (!this.entityWorld.isClient || this.entityWorld.isChunkLoaded(
				ChunkSectionPos.getSectionCoord(blockPos.x),
				ChunkSectionPos.getSectionCoord(blockPos.z)
			)
		) {
			vec3d.y - this.finalGravity
		} else if (this.y > this.entityWorld.bottomY) -0.1
		else 0.0

		if (this.noDrag) {
			this.setVelocity(vec3d.x, d, vec3d.z)
		} else {
			val h = if (this is Flutterer) g else 0.98f
			this.setVelocity(vec3d.x * g, d * h, vec3d.z * g)
		}
	}

	private fun travelInFluid(movementInput: Vec3d) {
		val bl = this.velocity.y <= 0.0
		val d = this.y
		val e: Double = this.finalGravity
		if (this.isTouchingWater) {
			this.travelInWater(movementInput, e, bl, d)
			this.floatIfRidden()
		} else {
			this.travelInLava(movementInput, e, bl, d)
		}
	}

	protected open fun travelInWater(movementInput: Vec3d, gravity: Double, falling: Boolean, y: Double) {
		val f = if (this.isSprinting) 0.9f else .8f
		val g = 0.02f

		this.updateVelocity(g, movementInput)
		this.move(MovementType.SELF, this.velocity)
		var vec3d = this.velocity
		if (this.horizontalCollision && this.isClimbing()) {
			vec3d = Vec3d(vec3d.x, 0.2, vec3d.z)
		}

		vec3d = vec3d.multiply(f.toDouble(), 0.8, f.toDouble())
		this.velocity = this.applyFluidMovingSpeed(gravity, falling, vec3d)
		this.resetVerticalVelocityInFluid(y)
	}

	private fun travelInLava(movementInput: Vec3d, gravity: Double, falling: Boolean, y: Double) {
		this.updateVelocity(0.02f, movementInput)
		this.move(MovementType.SELF, this.velocity)
		if (this.getFluidHeight(FluidTags.LAVA) <= this.swimHeight) {
			this.velocity = this.velocity.multiply(0.5, 0.8, 0.5)
			val vec3d: Vec3d = this.applyFluidMovingSpeed(gravity, falling, this.velocity)
			this.velocity = vec3d
		} else {
			this.velocity = this.velocity.multiply(0.5)
		}

		if (gravity != 0.0) {
			this.velocity = this.velocity.add(0.0, -gravity / 4.0, 0.0)
		}

		this.resetVerticalVelocityInFluid(y)
	}

	private fun resetVerticalVelocityInFluid(y: Double) {
		val vec3d = this.velocity
		if (this.horizontalCollision && this.doesNotCollide(vec3d.x, vec3d.y + 0.6f - this.y + y, vec3d.z)) {
			this.setVelocity(vec3d.x, 0.3, vec3d.z)
		}
	}

	private fun floatIfRidden() {
		val bl = this.type.isIn(EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN)
		if (bl && this.hasPassengers() && this.getFluidHeight(FluidTags.WATER) > this.swimHeight) {
			this.velocity = this.velocity.add(0.0, 0.04, 0.0)
		}
	}

	private fun applyMovementInput(movementInput: Vec3d, slipperiness: Float): Vec3d {
		this.updateVelocity(this.getMovementSpeed(slipperiness), movementInput)
		this.velocity = this.applyClimbingSpeed(this.velocity)
		this.move(MovementType.SELF, this.velocity)
		var vec3d = this.velocity
		if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.wasInPowderSnow && PowderSnowBlock.canWalkOnPowderSnow(
				this
			))
		) {
			vec3d = Vec3d(vec3d.x, 0.2, vec3d.z)
		}

		return vec3d
	}

	fun applyFluidMovingSpeed(gravity: Double, falling: Boolean, motion: Vec3d): Vec3d {
		if (gravity != 0.0 && !this.isSprinting) {
			val d = if (falling && abs(motion.y - 0.005) >= 0.003 && abs(motion.y - gravity / 16.0) < 0.003) {
				-0.003
			} else motion.y - gravity / 16.0

			return Vec3d(motion.x, d, motion.z)
		} else {
			return motion
		}
	}

	private fun applyClimbingSpeed(motion: Vec3d): Vec3d {
		return if (this.isClimbing()) {
			this.onLanding()
			val v = 0.15
			val x = MathHelper.clamp(motion.x, -v, v)
			val z = MathHelper.clamp(motion.z, -v, v)
			val y =
				if (motion.y < 0.0 && !this.blockStateAtPos.isOf(Blocks.SCAFFOLDING) && this.isHoldingOntoLadder()) 0.0
				else max(motion.y, -v)

			Vec3d(x, y, z)
		} else motion
	}

	private fun getMovementSpeed(slipperiness: Float): Float {
		return if (this.isOnGround) this.movementSpeed * (0.21600002f / (slipperiness * slipperiness * slipperiness)) else this.getOffGroundSpeed()
	}

	protected open fun getOffGroundSpeed(): Float {
		return if (this.isSprinting) 0.026f else 0.02f
	}

	private fun travelControlled(controllingPlayer: PlayerEntity?, movementInput: Vec3d?) {
//		val vec3d: Vec3d? = this.getControlledMovementInput(controllingPlayer, movementInput)
//		this.tickControlled(controllingPlayer, vec3d)
//		if (this.canMoveVoluntarily()) {
//			this.movementSpeed = (this.getSaddledSpeed(controllingPlayer))
//			this.travel(vec3d)
//		} else {
//			this.velocity = Vec3d.ZERO
//		}
	}

	override fun tick() {
		super.tick()
		tickMovement()
	}

	protected open fun getJumpVelocity(): Float {
		return this.getJumpVelocity(1.0f)
	}

	protected fun getJumpVelocity(strength: Float): Float {
		return .42f * strength * this.jumpVelocityMultiplier + this.getJumpBoostVelocityModifier()
	}

	fun getJumpBoostVelocityModifier(): Float {
		return 0.0f
	}

	@VisibleForTesting
	open fun jump() {
		val f: Float = this.getJumpVelocity()
		if (!(f <= 1.0E-5f)) {
			val vec3d = this.velocity
			this.setVelocity(vec3d.x, max(f.toDouble(), vec3d.y), vec3d.z)
			if (this.isSprinting) {
				val g = this.yaw * (Math.PI / 180.0).toFloat()
				this.addVelocityInternal(
					Vec3d(
						-MathHelper.sin(g.toDouble()) * 0.2,
						0.0,
						MathHelper.cos(g.toDouble()) * 0.2
					)
				)
			}

			this.velocityDirty = true
		}
	}

	open fun tickMovement() {
		if (this.jumpingCooldown > 0) {
			this.jumpingCooldown--
		}

		if (this.isInterpolating) {
			this.interpolator?.tick()
		} else if (!this.canMoveVoluntarily()) {
			this.velocity = this.velocity.multiply(0.98)
		}

		if (this.headTrackingIncrements > 0) {
			this.lerpHeadYaw(this.headTrackingIncrements, this.serverHeadYaw)
			this.headTrackingIncrements--
		}

//		this.equipment.tick(this)
		val vec3d = this.velocity
		var d = vec3d.x
		var e = vec3d.y
		var f = vec3d.z
		if (vec3d.horizontalLengthSquared() < 9.0E-6) {
			d = 0.0
			f = 0.0
		}

		if (abs(vec3d.y) < 0.003) {
			e = 0.0
		}
		this.setVelocity(d, e, f)

		val profiler = Profilers.get()
		profiler.push("ai")
		this.tickMovementInput()
		profiler.pop()

		profiler.push("jump")
		if (this.jumping) {
			val g: Double = if (this.isInLava) {
				this.getFluidHeight(FluidTags.LAVA)
			} else {
				this.getFluidHeight(FluidTags.WATER)
			}

			val inWater = this.isTouchingWater && g > 0.0
			val swimHeight = this.swimHeight
			if (!inWater || this.isOnGround && !(g > swimHeight)) {
				if (!this.isInLava || this.isOnGround && !(g > swimHeight)) {
					if ((this.isOnGround || inWater && g <= swimHeight) && this.jumpingCooldown == 0) {
						this.jump()
						this.jumpingCooldown = 10
					}
				} else {
					this.velocity = velocity.add(0.0, 0.04, 0.0)
				}
			} else {
				this.velocity = velocity.add(0.0, 0.04, 0.0)
			}
		} else {
			this.jumpingCooldown = 0
		}
		profiler.pop()

		profiler.push("travel")
		val vec3d2 = Vec3d(this.sidewaysSpeed.toDouble(), this.upwardSpeed.toDouble(), this.forwardSpeed.toDouble())

		if (this.controllingPassenger is DNDEntity && this.isAlive) {
			//this.travelControlled(playerEntity, vec3d2)
		} else if (this.canMoveVoluntarily() && this.canActVoluntarily()) {
			this.travel(vec3d2)
		}

		if (!this.entityWorld.isClient || this.isLogicalSideForUpdatingMovement) {
			this.tickBlockCollision()
		}

		if (entityWorld.isClient) {
			//val distance = MathHelper.magnitude(x - lastX, y - lastY, z - lastZ).toFloat()
			limbAnimator.updateLimbs(min(1f * 4f, 1f), 0.4f, .75f / hipHeight)
		}
		else {
			sendEquipmentUpdates()
		}
		profiler.pop()
		this.headYaw = this.yaw
	}

	private fun tickMovementInput() {
		if (entityWorld.isClient) {
			val character = character ?: return
			val engine = entityWorld.getEngine()
			engine.getControllingPlayer(character.uuid)
//			if ()
//			val vec2f: Vec2f = this.applyMovementSpeedFactors(this.input.getMovementInput())
//			this.sidewaysSpeed = vec2f.x
//			this.forwardSpeed = vec2f.y
//			this.jumping = this.input.playerInput.jump()
//			this.lastRenderYaw = this.renderYaw
//			this.lastRenderPitch = this.renderPitch
//			this.renderPitch = this.renderPitch + (this.getPitch() - this.renderPitch) * 0.5f
//			this.renderYaw = this.renderYaw + (this.getYaw() - this.renderYaw) * 0.5f
		}
	}

	private fun lerpHeadYaw(headTrackingIncrements: Int, serverHeadYaw: Double) {
		this.headYaw =
			MathHelper.lerpAngleDegrees(1.0 / headTrackingIncrements, this.headYaw.toDouble(), serverHeadYaw).toFloat()
	}

	override fun baseTick() {
		super.baseTick()
		lastHeadYaw = entityHeadYaw
		lastBodyYaw = entityBodyYaw
		lastPitch = pitch
		lastYaw = yaw
	}

	private fun sendEquipmentUpdates() {
		val changes = getEquipmentChanges()

		if(!changes.isEmpty()) {
			val world = entityWorld as? ServerWorld ?: return
			val payload = EntityDNDEquipmentUpdateS2CPayload(id, changes)
			for(player in world.players) ServerPlayNetworking.send(player, payload)
		}
	}

	private fun getEquipmentChanges(): Map<Int, ItemStack> {
		val inventory = character?.inventory ?: return emptyMap()
		val changes = mutableMapOf<Int, ItemStack>()
		for(i in 0..19){
			val equipped = inventory.equipment[i].copy()
			if(!ItemStack.areItemsEqual(equipped, lastEquippedStacks[i])){
				changes[i] = equipped
				lastEquippedStacks[i] = equipped
			}
		}
		return changes
	}

	override fun getMovement(): Vec3d? {
		val entity = this.vehicle
		return if (entity != null && entity.controllingPassenger != this) entity.movement else this.movement

	}

	fun setMovement(movement: Vec3d) {
		this.movement = movement
	}

	override fun getHeadYaw(): Float = entityHeadYaw
	override fun getBodyYaw(): Float = entityBodyYaw
	override fun setHeadYaw(headYaw: Float) { this.entityHeadYaw = headYaw }
	override fun setBodyYaw(bodyYaw: Float) { this.entityBodyYaw = bodyYaw }

	override fun damage(world: ServerWorld, source: DamageSource, amount: Float): Boolean {
		return false
	}

	override fun getGravity(): Double {
		return 0.08
	}

	override fun isCollidable(entity: Entity?): Boolean {
		return entity is DNDEntity
	}

	fun isHoldingOntoLadder() = isSneaking

	fun hasLandedInFluid(): Boolean {
		return this.velocity.y < 1.0E-5f && this.isInFluid
	}

	fun canBeHit(attacker: Character): Boolean {
		return attacker.engine.running && attacker != character
	}

	override fun canMoveVoluntarily(): Boolean {
		val player = entityWorld.getEngine().getControllingPlayer(character?.uuid ?: return !entityWorld.isClient)
		return !entityWorld.isClient || player?.isMainPlayer == true
	}

	override fun canActVoluntarily(): Boolean = canMoveVoluntarily()

	override fun isControlledByMainPlayer(): Boolean {
		return false
	}

	override fun isControlledByPlayer(): Boolean {
		val engine = entityWorld.getEngine()
		return engine.running && engine.playerEntities.contains(this)
	}

	override fun getDimensions(pose: EntityPose): EntityDimensions {
		val model = character?.model ?: return super.getDimensions(pose)
		return EntityDimensions.fixed(model.width, model.height).withEyeHeight(model.eyeY)
	}

	override fun readCustomData(view: ReadView) {
		val (most, least) = view.getOptionalLongArray("characterUUID").getOrNull() ?: return
		val uuid = UUID(most, least)

		// The data tracker is synced with the client
		dataTracker.set(CHARACTER_UUID, uuid)
		// This is for the server
		character = entityWorld.getEngine().getCharacter(uuid) ?: character
		calculateDimensions()
	}

	override fun writeCustomData(view: WriteView) {
		val uuid = character?.uuid ?: return
		view.putLongArray("characterUUID", longArrayOf(uuid.mostSignificantBits, uuid.leastSignificantBits))
	}

	companion object {
		val CHARACTER_UUID: TrackedData<UUID> =
			DataTracker.registerData(DNDEntity::class.java, MTTEntities.UUID_TRACKER)
		val CONTROLLING_PLAYER = DataTracker.registerData(DNDEntity::class.java, MTTEntities.OPTIONAL_UUID_TRACKER)
	}
}