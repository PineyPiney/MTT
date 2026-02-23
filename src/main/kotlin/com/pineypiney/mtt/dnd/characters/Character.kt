package com.pineypiney.mtt.dnd.characters

import com.mojang.logging.LogUtils
import com.pineypiney.mtt.component.DamageRolls
import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.conditions.ConditionManager
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.rolls.AbilityCheck
import com.pineypiney.mtt.dnd.rolls.SavingThrow
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.ArmourType
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.dnd.traits.proficiencies.WeaponType
import com.pineypiney.mtt.entity.DNDInventory
import com.pineypiney.mtt.item.dnd.DNDGameItem
import com.pineypiney.mtt.item.dnd.equipment.DNDShieldItem
import com.pineypiney.mtt.item.dnd.equipment.DNDWeaponItem
import com.pineypiney.mtt.network.payloads.s2c.CharacterDamageS2CPayload
import com.pineypiney.mtt.util.D20
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtList
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.storage.NbtReadView
import net.minecraft.storage.NbtWriteView
import net.minecraft.util.ErrorReporter
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.max
import kotlin.math.min

abstract class Character(val uuid: UUID) {

	abstract val engine: DNDEngine<*>
	abstract val details: CharacterDetails

	var name: String
		get() = details.name
		set(value) {
			details.name = value
		}
	val race: Race get() = details.race
	val type: CreatureType get() = details.type
	val size: Size get() = details.size
	val speed: Int get() = details.speed
	val model: CharacterModel get() = details.model
	var health: Int
		get() = details.health
		set(value) {
			details.health = value
		}
	val maxHealth: Int get() = details.maxHealth
	val abilities: Abilities get() = details.abilities
	var baseArmourClass: Int
		get() = details.armourClass
		set(value) {
			details.armourClass = value
		}
	val inventory: DNDInventory = DNDInventory(this)
	val conditions: ConditionManager by lazy { ConditionManager(this, details.conditions) }

	var world: RegistryKey<World> = World.OVERWORLD
	var pos = Vec3d(0.0, 0.0, 0.0)
	var yaw = 0f
	var pitch = 0f

	fun getTotalArmour(): Int{
		val armour = inventory.getArmour()
		val shield = inventory.getOffhand() as? DNDShieldItem

		// Armour from worn armour and shield
		var total = armour?.let { maxOf(it.armourClass, baseArmourClass) } ?: baseArmourClass
		if(shield != null) total += shield.armourClass

		// Armour from Dexterity
		total += if(armour == null) abilities.dexMod
		else when(armour.armourType){
			ArmourType.LIGHT -> abilities.dexMod
			ArmourType.MEDIUM -> min(abilities.dexMod, 2)
			ArmourType.HEAVY -> min(abilities.dexMod, 0)
		}
		return total
	}

	open fun getInitiativeModifier(): Int {
		return abilities.dexMod
	}

	fun getLevel(): Int = details.level

	fun getPreparedSpells(): Set<Spell> = details.getPreparedSpells()

	fun isProficientIn(equipment: EquipmentType): Boolean = details.isProficientIn(equipment)

	fun isProficientIn(ability: Ability): Boolean = details.isProficientIn(ability)

	fun getProficiencyBonus(): Int = details.getProficiencyBonus()

	fun getDamage(): DamageRolls {
		val stack = inventory.getHeldStack()
		return DNDWeaponItem.getDamage(stack, this)
	}

	open fun attack(target: Character) {
		val armour = target.getTotalArmour()
		val roll = D20.roll()
		val crit = roll == 20
		if (crit || roll >= armour) {
			target.damage(getDamage(), crit, this)
		}
	}

	open fun damage(damage: DamageRolls, crit: Boolean, attacker: Character?, savingThrow: SavingThrow? = null) {
		val saved = savingThrow?.roll(this) ?: false
		for (damage in damage.types) {
			var amount = damage.roll(crit, attacker != null && attacker.inventory.getOffhand() == null)
			if (saved) amount /= 2
			if (amount > 0) {
				if (engine is ServerDNDEngine) engine.updates.add(CharacterDamageS2CPayload(uuid, damage.type, amount))
			}
		}
	}

	open fun rollSavingThrow(savingThrow: SavingThrow): Boolean = savingThrow.roll(this)

	open fun rollAbilityCheck(check: AbilityCheck): Int = check.roll(this)

	open fun rollInitiative(): Int = AbilityCheck.INITIATIVE.roll(this)

	fun getAttackBonus(weaponType: WeaponType, stack: ItemStack): Int {
		var i = if (weaponType.finesse) max(abilities.strMod, abilities.dexMod) else abilities.strMod
		if (isProficientIn(weaponType)) i += getProficiencyBonus()
		i += stack[MTTComponents.HIT_BONUS_TYPE] ?: 0
		return i
	}

	fun getDamageBonus(weaponType: WeaponType, stack: ItemStack): Int {
		var i = if (weaponType.finesse) max(abilities.strMod, abilities.dexMod) else abilities.strMod
		i += stack[MTTComponents.DAMAGE_BONUS_TYPE] ?: 0
		return i
	}

	fun addItemStack(stack: ItemStack) {
		inventory.insertStack(-1, stack)
		(stack.item as? DNDGameItem)?.addToCharacter(this, stack)
	}

	fun readNbt(nbt: NbtCompound, regManager: DynamicRegistryManager) {
		val posArray = nbt.getListOrEmpty("pos")
		pos = Vec3d(
			posArray.getDouble(0, 0.0),
			posArray.getDouble(1, 0.0),
			posArray.getDouble(2, 0.0)
		)

		val worldID = Identifier.of(nbt.getString("world").get())
		world = RegistryKey.of(RegistryKeys.WORLD, worldID)

		val reporter = ErrorReporter.Logging(getErrorReporterContext(), LogUtils.getLogger())
		val view = NbtReadView.create(reporter, regManager, nbt.getCompoundOrEmpty("inventory"))
		inventory.readNbt(view)
	}

	fun writeNbt(nbt: NbtCompound, regManager: DynamicRegistryManager) {
		val posNbt = NbtList()
		posNbt.add(NbtDouble.of(pos.x))
		posNbt.add(NbtDouble.of(pos.y))
		posNbt.add(NbtDouble.of(pos.z))
		nbt.put("pos", posNbt)


		nbt.putString("world", world.value.toString())

		val reporter = ErrorReporter.Logging(getErrorReporterContext(), LogUtils.getLogger())
		val view = NbtWriteView.create(reporter, regManager)
		inventory.writeNbt(view)
		nbt.put("inventory", view.nbt)
	}

	fun createPayload(regManager: DynamicRegistryManager): CustomPayload {
		val nbt = NbtCompound()
		writeNbt(nbt, regManager)
		return details.createPayload(regManager, uuid, nbt)
	}

	fun isPlayable() = details is CharacterSheet

	fun getErrorReporterContext() = ErrorReportingContext(this)

	override fun toString(): String = "$name($uuid)"

	data class ErrorReportingContext(val character: Character) : ErrorReporter.Context {
		override fun getName(): String = character.name
	}
}