package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.ArmourType
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.entity.DNDInventory
import com.pineypiney.mtt.item.dnd.DNDItem
import com.pineypiney.mtt.item.dnd.equipment.DNDShieldItem
import com.pineypiney.mtt.screen.DNDScreenHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.RegistryByteBuf
import net.minecraft.registry.RegistryKey
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.min

abstract class Character(val uuid: UUID) : NamedScreenHandlerFactory {
	abstract val name: String
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
		var total = armour?.let { maxOf(it.armourClass, baseArmourClass) } ?: baseArmourClass
		if(shield != null) total += shield.armourClass
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

	abstract fun createEntity(world: World): DNDEntity

	abstract fun save(buf: RegistryByteBuf)

	override fun getDisplayName(): Text {
		return Text.literal(name)
	}
	override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler? {
		return DNDScreenHandler(syncId, playerInventory, inventory)
	}
}