package com.pineypiney.mtt.entity

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.TempCharacter
import com.pineypiney.mtt.network.payloads.s2c.EntityDNDEquipmentUpdateS2CPayload
import com.pineypiney.mtt.util.getEngine
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LimbAnimator
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.math.abs
import kotlin.math.min

abstract class DNDEntity(type: EntityType<*>, world: World) : Entity(type, world) {

	var character: Character = TempCharacter

	var hipHeight: Float = .75f
	val limbAnimator = LimbAnimator()
	var entityBodyYaw: Float = 0f
	var lastBodyYaw: Float = 0f
	var entityHeadYaw: Float = 0f
	var lastHeadYaw: Float = 0f
	val lastEquippedStacks = Array(20){ ItemStack.EMPTY }

	var name: String
		get() = customName?.string ?: "Unnamed Character"
		set(value) { customName = Text.literal(value) }

	override fun initDataTracker(builder: DataTracker.Builder) {
		builder.add(CHARACTER_UUID, UUID.randomUUID())
	}

	override fun onDataTrackerUpdate(entries: List<DataTracker.SerializedEntry<*>?>?) {
		val engine = world.getEngine()
		character = engine.getCharacter(dataTracker.get(CHARACTER_UUID)) ?: character
	}

	override fun tick() {
		super.tick()
		//turnHead()
		if(world.isClient) {
			//val distance = MathHelper.magnitude(x - lastX, y - lastY, z - lastZ).toFloat()
			limbAnimator.updateLimbs(min(1f * 4f, 1f), 0.4f, .75f / hipHeight)
		}
		else {
			sendEquipmentUpdates()
		}
	}

	override fun baseTick() {
		super.baseTick()
		lastHeadYaw = entityHeadYaw
		lastBodyYaw = entityBodyYaw
		lastPitch = pitch
		lastYaw = yaw
	}

	fun sendEquipmentUpdates(){
		val changes = getEquipmentChanges()

		if(!changes.isEmpty()) {
			val world = world as? ServerWorld ?: return
			val payload = EntityDNDEquipmentUpdateS2CPayload(id, changes)
			for(player in world.players) ServerPlayNetworking.send(player, payload)
		}
	}

	fun getEquipmentChanges(): Map<Int, ItemStack>{
		val changes = mutableMapOf<Int, ItemStack>()
		for(i in 0..19){
			val equipped = character.inventory.equipment[i].copy()
			if(!ItemStack.areItemsEqual(equipped, lastEquippedStacks[i])){
				changes[i] = equipped
				lastEquippedStacks[i] = equipped
			}
		}
		return changes
	}

	override fun getHeadYaw(): Float = entityHeadYaw
	override fun getBodyYaw(): Float = entityBodyYaw
	override fun setHeadYaw(headYaw: Float) { this.entityHeadYaw = headYaw }
	override fun setBodyYaw(bodyYaw: Float) { this.entityBodyYaw = bodyYaw }

	protected open fun turnHead(bodyRotation: Float) {
		val f = MathHelper.wrapDegrees(bodyRotation - this.entityBodyYaw)
		this.entityBodyYaw += f * 0.3f
		val g = MathHelper.wrapDegrees(this.yaw - this.entityBodyYaw)
		val h = 50f
		if (abs(g) > h) {
			this.entityBodyYaw += (g - MathHelper.sign(g.toDouble()) * h)
		}
	}

	override fun damage(world: ServerWorld, source: DamageSource, amount: Float): Boolean {
		return false
	}

	override fun getGravity(): Double {
		return 0.08
	}

	override fun isCollidable(): Boolean {
		return false
	}

	override fun readCustomDataFromNbt(nbt: NbtCompound) {
		val (most, least) = nbt.getLongArray("characterUUID").getOrNull() ?: return
		val uuid = UUID(most, least)

		// The data tracker is synced with the client
		dataTracker.set(CHARACTER_UUID, uuid)
		// This is for the server
		character = world.getEngine().getCharacter(uuid) ?: character
	}

	override fun writeCustomDataToNbt(nbt: NbtCompound) {
		nbt.putLongArray("characterUUID", longArrayOf(character.uuid.mostSignificantBits, character.uuid.leastSignificantBits))
	}

	companion object {
		val CHARACTER_UUID = DataTracker.registerData(DNDEntity::class.java, MTTEntities.UUID_TRACKER)
	}
}