package com.pineypiney.mtt.item.dnd

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.CoinValue
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Rarity
import net.minecraft.item.ItemStack

abstract class DNDGameItem(settings: Settings) : DNDItem(settings) {
	abstract val value: CoinValue
	abstract val weight: Float
	abstract val rarity: Rarity

	open fun addToCharacter(character: Character, stack: ItemStack) {}

	companion object {
		fun getValue(stack: ItemStack): CoinValue {
			val item = stack.item
			if (item !is DNDGameItem) return CoinValue.ZERO
			return stack.components[MTTComponents.VALUE_OVERRIDE_TYPE] ?: item.value
		}

		fun getWeight(stack: ItemStack): Float {
			val item = stack.item
			if (item !is DNDGameItem) return 0f
			return stack.components[MTTComponents.WEIGHT_OVERRIDE_TYPE] ?: item.weight
		}

		fun getRarity(stack: ItemStack): Rarity {
			val item = stack.item
			if (item !is DNDGameItem) return Rarity.COMMON
			return stack.components[MTTComponents.RARITY_OVERRIDE_TYPE]?.let { Rarity.getLevel(it) } ?: item.rarity
		}
	}
}