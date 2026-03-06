package com.pineypiney.mtt.dnd.characters

import com.mojang.logging.LogUtils
import com.pineypiney.mtt.component.DamageRolls
import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dice.DieRoll
import com.pineypiney.mtt.dice.RollResult
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.conditions.ConditionManager
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.rolls.AbilityCheck
import com.pineypiney.mtt.dnd.rolls.AttackRoll
import com.pineypiney.mtt.dnd.rolls.SavingThrow
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.dnd.traits.proficiencies.WeaponType
import com.pineypiney.mtt.entity.DNDInventory
import com.pineypiney.mtt.item.dnd.DNDGameItem
import com.pineypiney.mtt.item.dnd.equipment.DNDMeleeItem
import com.pineypiney.mtt.item.dnd.equipment.DNDShieldItem
import com.pineypiney.mtt.item.dnd.equipment.DNDWeaponItem
import com.pineypiney.mtt.network.payloads.s2c.CharacterDamageS2CPayload
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtList
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.scoreboard.ScoreHolder
import net.minecraft.storage.NbtReadView
import net.minecraft.storage.NbtWriteView
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.ErrorReporter
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.max

abstract class Character(val uuid: UUID) : ScoreHolder {

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
		else armour.armourType.dexMod(abilities.dexMod)
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
		val attackRoll = rollAttackRoll(AttackRoll(target.inventory.getHeldStack()), armour)

		if (attackRoll.crit || attackRoll >= armour) {
			target.damage(getDamage(), attackRoll.crit, this)
		}
	}

	open fun damage(damage: DamageRolls, crit: Boolean, attacker: Character?, savingThrow: Pair<SavingThrow, Int>? = null) {
		val saved: Boolean
		if (savingThrow != null) {
			val (save, target) = savingThrow
			val roll = save.roll(this)
			saved = roll >= target
		} else saved = false

		for (damage in damage.types) {
			var amount = damage.roll(crit, attacker != null && attacker.inventory.getOffhand() == null)
			if (saved) amount /= 2
			if (amount > 0) {
				if (engine is ServerDNDEngine) engine.updates.add(CharacterDamageS2CPayload(uuid, damage.type, amount))
			}
		}
	}

	open fun rollSavingThrow(savingThrow: SavingThrow, target: Int, roll: DieRoll = DieRoll.d20()): RollResult = savingThrow.roll(this, roll)

	open fun rollAbilityCheck(check: AbilityCheck, target: Int, roll: DieRoll = DieRoll.d20()): RollResult = check.roll(this, roll)

	open fun rollInitiative(roll: DieRoll = DieRoll.d20()): RollResult = AbilityCheck.INITIATIVE.roll(this, roll)

	open fun rollAttackRoll(roll: AttackRoll, target: Int, dieRoll: DieRoll = DieRoll.d20()): RollResult = roll.roll(this, dieRoll)

	fun getAttackBonus(stack: ItemStack): Int {
		if (stack.isEmpty) return abilities.strMod + getProficiencyBonus()

		val item = stack.item
		return if (item is DNDMeleeItem) getAttackBonus(item.weaponType, stack)
		else abilities.strMod + (stack[MTTComponents.HIT_BONUS_TYPE] ?: 0)
	}

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

	fun isPlayable() = true

	fun getCombat() = engine.getCombat(this)

	fun getErrorReporterContext() = ErrorReportingContext(this)

	override fun getNameForScoreboard(): String = name
	override fun getDisplayName(): MutableText = Text.literal(name)

	override fun toString(): String = "$name($uuid)"

	data class ErrorReportingContext(val character: Character) : ErrorReporter.Context {
		override fun getName(): String = character.name
	}
}