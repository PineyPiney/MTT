package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.Damage
import com.pineypiney.mtt.dnd.DamageType
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.ArmourType
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
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.min

abstract class Character(val uuid: UUID, val engine: DNDEngine) : NamedScreenHandlerFactory {

	abstract var name: String
	abstract val race: Race
	abstract val type: CreatureType
	abstract val size: Size
	abstract val speed: Int
	abstract val model: String
	abstract var health: Int
	abstract val maxHealth: Int
	abstract val abilities: Abilities
	abstract var baseArmourClass: Int
	val inventory: DNDInventory = DNDInventory()

	var world: RegistryKey<World> = World.OVERWORLD
	var pos = Vec3d(0.0, 0.0, 0.0)

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

	fun getDamage(): Set<Damage> {
		val stack = inventory.getHeldStack()
		val weapon = stack.item
		return if (weapon !is DNDWeaponItem) {
			setOf(Damage(abilities.strMod + 1, DamageType.BLUDGEONING))
		} else setOf(
			Damage(
				Damage.Roll(weapon.weaponType.numDice, weapon.weaponType.sides),
				0,
				weapon.weaponType.damageType
			)
		)
	}

	fun attack(target: Character) {
		val armour = target.getTotalArmour()
		val roll = D20.roll()
		val crit = roll == 20
		if (crit || roll >= armour) {
			hit(target, getDamage(), crit)
		}
	}

	fun hit(target: Character, damages: Set<Damage>, crit: Boolean) {
		if (engine is DNDServerEngine) {
			val player = engine.getControllingPlayer(uuid) ?: return
			for (damage in damages) {
				val amount = damage.roll(crit)
				player.sendMessage(
					Text.literal("Dealt $amount ")
						.append(damage.type.getText())
						.append(" damage to ${target.name} who is ${(target.pos.distanceTo(this.pos))} blocks away"),
					false
				)
			}
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

		val inv = nbt.getListOrEmpty("inventory")
		inventory.readNbt(inv, regManager)
	}

	fun writeNbt(nbt: NbtCompound, regManager: DynamicRegistryManager) {
		val posNbt = NbtList()
		posNbt.add(NbtDouble.of(pos.x))
		posNbt.add(NbtDouble.of(pos.y))
		posNbt.add(NbtDouble.of(pos.z))
		nbt.put("pos", posNbt)

		nbt.putString("world", world.value.toString())
		nbt.put("inventory", inventory.writeNbt(regManager))
	}

	abstract fun createEntity(world: World): DNDEntity

	abstract fun createPayload(regManager: DynamicRegistryManager): CustomPayload

	override fun getDisplayName(): Text {
		return Text.literal(name)
	}
	override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler? {
		return DNDScreenHandler(syncId, playerInventory, inventory)
	}
}