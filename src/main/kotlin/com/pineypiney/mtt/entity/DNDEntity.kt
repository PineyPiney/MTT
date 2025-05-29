package com.pineypiney.mtt.entity

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.item.dnd.DNDItem
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LimbAnimator
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.world.World
import kotlin.math.min

abstract class DNDEntity(type: EntityType<*>, world: World) : Entity(type, world) {

	abstract val character: Character
	var species: Species = Species.NONE
	var hipHeight: Float = .75f
	val limbAnimator = LimbAnimator()

	var name: String
		get() = customName?.string ?: "Unnamed Character"
		set(value) { customName = Text.literal(value) }

	var inCombat: Boolean
		get() = dataTracker.get(IN_COMBAT)
		set(value) { dataTracker.set(IN_COMBAT, value) }

	override fun initDataTracker(builder: DataTracker.Builder) {
		builder.add(MAX_HEALTH, 10)
		builder.add(CURRENT_HEALTH, 10)
		builder.add(IN_COMBAT, false)
	}

	override fun tick() {
		super.tick()
		//val distance = MathHelper.magnitude(x - lastX, y - lastY, z - lastZ).toFloat()
		limbAnimator.updateLimbs(min(1f * 4f, 1f), 0.4f, .75f / hipHeight)
	}

	fun addItemStack(stack: ItemStack){
		character.inventory.insertStack(-1, stack)
		(stack.item as? DNDItem)?.addToCharacter(this, stack)
	}

	fun calculateCarryCapacity() = 15 * character.abilities.strength
	abstract fun calculateProficiencyBonus(): Int

	override fun damage(
		world: ServerWorld?,
		source: DamageSource?,
		amount: Float
	): Boolean {
		return false
	}

	override fun getGravity(): Double {
		return 0.08
	}

	override fun isCollidable(): Boolean {
		return true
	}

	override fun readCustomDataFromNbt(nbt: NbtCompound) {
		val inventoryData = nbt.getListOrEmpty("Inventory")
		character.inventory.readNbt(inventoryData, world.registryManager)
	}

	override fun writeCustomDataToNbt(nbt: NbtCompound) {
		nbt.put("Inventory", character.inventory.writeNbt(world.registryManager))
	}

	companion object {
		val IN_COMBAT = DataTracker.registerData(DNDEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)
		val MAX_HEALTH = DataTracker.registerData(DNDEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
		val CURRENT_HEALTH = DataTracker.registerData(DNDEntity::class.java, TrackedDataHandlerRegistry.INTEGER)
	}
}