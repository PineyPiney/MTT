package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.network.ServerDNDEntity
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.network.payloads.s2c.CharacterSheetS2CPayload
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.world.World
import java.util.*

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
		val entity = ServerDNDEntity(world, this)
		return entity
	}

	override fun createPayload(regManager: DynamicRegistryManager): CustomPayload {
		val nbt = NbtCompound()
		writeNbt(nbt, regManager)
		return CharacterSheetS2CPayload(uuid, sheet, nbt)
	}

	override fun getLevel(): Int = sheet.level

	override fun isProficientIn(equipment: EquipmentType): Boolean = sheet.isProficientIn(equipment)
	override fun getProficiencyBonus(): Int = sheet.calculateProficiencyBonus()

	override fun toString(): String {
		return "$name($race, (${sheet.classes.entries.joinToString { (dndClass, level) -> "$dndClass $level" }}))"
	}
}