package com.pineypiney.mtt.dnd.characters

import com.mojang.logging.LogUtils
import com.pineypiney.mtt.component.DamageRolls
import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.ArmourType
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.dnd.traits.proficiencies.WeaponType
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.entity.DNDInventory
import com.pineypiney.mtt.item.dnd.DNDItem
import com.pineypiney.mtt.item.dnd.equipment.DNDShieldItem
import com.pineypiney.mtt.item.dnd.equipment.DNDWeaponItem
import com.pineypiney.mtt.screen.DNDScreenHandler
import com.pineypiney.mtt.util.D20
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtList
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.storage.NbtReadView
import net.minecraft.storage.NbtWriteView
import net.minecraft.text.Text
import net.minecraft.util.ErrorReporter
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.max
import kotlin.math.min

abstract class Character(val uuid: UUID, val engine: DNDEngine) : NamedScreenHandlerFactory {

	abstract var name: String
	abstract val race: Race
	abstract val type: CreatureType
	abstract val size: Size
	abstract val speed: Int
	abstract val model: CharacterModel
	abstract var health: Int
	abstract val maxHealth: Int
	abstract val abilities: Abilities
	abstract var baseArmourClass: Int
	val inventory: DNDInventory = DNDInventory()

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

	open fun getInitiative(): Int{
		return abilities.dexMod
	}

	fun addItemStack(stack: ItemStack){
		inventory.insertStack(-1, stack)
		(stack.item as? DNDItem)?.addToCharacter(this, stack)
	}

	fun getDamage(): DamageRolls {
		val stack = inventory.getHeldStack()
		return DNDWeaponItem.getDamage(stack, this)
	}

	fun attack(target: Character) {
		val armour = target.getTotalArmour()
		val roll = D20.roll()
		val crit = roll == 20
		if (crit || roll >= armour) {
			target.damage(getDamage(), crit, this)
		}
	}

	fun damage(damage: DamageRolls, crit: Boolean, attacker: Character?) {
		for (damage in damage.types) {
			damage.roll(crit, attacker != null && attacker.inventory.getOffhand() == null)
		}
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

	abstract fun createEntity(world: World): DNDEntity

	abstract fun createPayload(regManager: DynamicRegistryManager): CustomPayload

	abstract fun getLevel(): Int

	abstract fun isProficientIn(equipment: EquipmentType): Boolean

	abstract fun getProficiencyBonus(): Int

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

	override fun getDisplayName(): Text {
		return Text.literal(name)
	}
	override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler? {
		return DNDScreenHandler(syncId, playerInventory, inventory)
	}

	fun getErrorReporterContext() = ErrorReportingContext(this)

	data class ErrorReportingContext(val character: Character) : ErrorReporter.Context {
		override fun getName(): String = character.name
	}
}