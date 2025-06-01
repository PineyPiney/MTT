package com.pineypiney.mtt.item.dnd

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Rarity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

abstract class DNDItem(settings: Settings) : Item(settings) {
	abstract val value: Int
	abstract val weight: Float
	abstract val rarity: Rarity

	open fun addToCharacter(character: Character, stack: ItemStack){}

	companion object {
		fun getValue(stack: ItemStack): Int{
			val item = stack.item
			if(item !is DNDItem) return 0
			return stack.components[MTTComponents.VALUE_OVERRIDE_TYPE] ?: item.value
		}
		fun getWeight(stack: ItemStack): Float{
			val item = stack.item
			if(item !is DNDItem) return 0f
			return stack.components[MTTComponents.WEIGHT_OVERRIDE_TYPE] ?: item.weight
		}
		fun getRarity(stack: ItemStack): Rarity{
			val item = stack.item
			if(item !is DNDItem) return Rarity.COMMON
			return stack.components[MTTComponents.RARITY_OVERRIDE_TYPE]?.let { Rarity.getLevel(it) } ?: item.rarity
		}
	}
}