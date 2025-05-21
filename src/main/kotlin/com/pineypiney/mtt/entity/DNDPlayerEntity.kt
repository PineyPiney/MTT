package com.pineypiney.mtt.entity

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.item.dnd.equipment.WeaponType
import com.pineypiney.mtt.screen.DNDScreenHandler
import com.pineypiney.mtt.serialisation.MTTCodecs
import com.pineypiney.mtt.util.optional
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.world.World
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.math.max

class DNDPlayerEntity(type: EntityType<*>, world: World): DNDClassEntity(type, world), NamedScreenHandlerFactory {

	val screenHandler = DNDScreenHandler(1, inventory)

	val weaponProficiencies = mutableListOf<WeaponType>()

	var controllingPlayer: UUID?
		get() = this.dataTracker[CONTROLLING_PLAYER].getOrNull()
		set(value) { dataTracker[CONTROLLING_PLAYER] = value.optional() }

	var creating = true
	var targetLevel = 1

	constructor(type: EntityType<*>, world: World, name: String): this(type, world){
		this.name = name
	}

	init {
		isCustomNameVisible = true
	}

	override fun initDataTracker(builder: DataTracker.Builder) {
		super.initDataTracker(builder)
		builder.add(CONTROLLING_PLAYER, Optional.empty())
	}

	fun getAttackBonus(weaponType: WeaponType, stack: ItemStack): Int{
		var i = if(weaponType.finesse) max(abilities.strMod, abilities.dexMod) else abilities.strMod
		if(weaponProficiencies.contains(weaponType)) i += calculateProficiencyBonus()
		i += stack[MTTComponents.HIT_BONUS_TYPE] ?: 0
		return i
	}

	fun getDamageBonus(weaponType: WeaponType, stack: ItemStack): Int{
		var i = if(weaponType.finesse) max(abilities.strMod, abilities.dexMod) else abilities.strMod
		i += stack[MTTComponents.DAMAGE_BONUS_TYPE] ?: 0
		return i
	}

	override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity?): ScreenHandler? {
		return DNDScreenHandler(syncId, playerInventory, inventory)
	}

	override fun writeCustomDataToNbt(nbt: NbtCompound) {
		super.writeCustomDataToNbt(nbt)
		controllingPlayer?.let { nbt.put("controlling", MTTCodecs.UUID_CODEC, it) }
	}

	override fun readCustomDataFromNbt(nbt: NbtCompound) {
		super.readCustomDataFromNbt(nbt)
		controllingPlayer = nbt.get("controlling", MTTCodecs.UUID_CODEC).getOrNull()
	}

	companion object {
		val CONTROLLING_PLAYER = DataTracker.registerData(DNDPlayerEntity::class.java, MTTEntities.OPTIONAL_UUID_TRACKER)
	}
}