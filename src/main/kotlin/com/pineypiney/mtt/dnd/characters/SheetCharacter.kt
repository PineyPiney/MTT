package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.WeaponType
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.network.payloads.s2c.CharacterSheetS2CPayload
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.world.World
import java.util.*
import kotlin.math.max

class SheetCharacter(val sheet: CharacterSheet, uuid: UUID, engine: DNDEngine) : Character(uuid, engine) {
	override var name: String
		get() = sheet.name
		set(value) { sheet.name = value }
	override val race: Race get() = sheet.race
	override val type: CreatureType get() = sheet.type
	override val size: Size get() = sheet.size
	override val speed: Int get() = sheet.speed
	override val model: CharacterModel get() = sheet.model
	override var health: Int
		get() = sheet.health
		set(value) { sheet.health = value }
	override val maxHealth: Int get() = sheet.maxHealth
	override val abilities: Abilities get() = sheet.abilities
	override var baseArmourClass: Int
		get() = sheet.armourClass
		set(value) { sheet.armourClass = value }

	override fun createEntity(world: World): DNDEntity {
		val entity = DNDEntity(world, this)
		return entity
	}

	override fun createPayload(regManager: DynamicRegistryManager): CustomPayload {
		val nbt = NbtCompound()
		writeNbt(nbt, regManager)
		return CharacterSheetS2CPayload(uuid, sheet, nbt)
	}

	fun getAttackBonus(weaponType: WeaponType, stack: ItemStack): Int{
		var i = if(weaponType.finesse) max(abilities.strMod, abilities.dexMod) else abilities.strMod
		if(sheet.isProficientIn(weaponType)) i += sheet.calculateProficiencyBonus()
		i += stack[MTTComponents.HIT_BONUS_TYPE] ?: 0
		return i
	}

	fun getDamageBonus(weaponType: WeaponType, stack: ItemStack): Int{
		var i = if(weaponType.finesse) max(abilities.strMod, abilities.dexMod) else abilities.strMod
		i += stack[MTTComponents.DAMAGE_BONUS_TYPE] ?: 0
		return i
	}

	override fun toString(): String {
		return "Sheet Character[$name, $uuid]"
	}
}