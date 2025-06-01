package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.entity.DNDEntity
import net.minecraft.network.RegistryByteBuf
import net.minecraft.world.World
import java.util.*

object TempCharacter : Character(UUID.randomUUID()){
	override val name: String get() = "temp"
	override val type: CreatureType get() = CreatureType.HUMANOID
	override val size: Size get() = Size.MEDIUM
	override val speed: Int get() = 0
	override val model: String get() = "default"
	override var health: Int
		get() = 0
		set(value) {}
	override val maxHealth: Int get() = 0
	override val abilities: Abilities = Abilities()
	override var baseArmourClass: Int
		get() = 0
		set(value) {}

	override fun createEntity(world: World): DNDEntity {
		throw Exception("Should not create entity from TempCharacter")
	}

	override fun save(buf: RegistryByteBuf) {

	}
}