package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.entity.DNDInventory

abstract class Character {
	abstract val type: CreatureType
	abstract val size: Size
	abstract val speed: Int
	abstract val abilities: Abilities
	val inventory: DNDInventory = DNDInventory()
}