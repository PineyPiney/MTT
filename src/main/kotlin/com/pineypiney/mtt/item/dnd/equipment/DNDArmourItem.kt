package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.Rarity
import com.pineypiney.mtt.item.dnd.ArmourType
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack

open class DNDArmourItem(settings: Settings, override val value: Int, override val weight: Float, val armourClass: Int, val armourType: ArmourType, val stealthDisadvantage: Boolean, override val rarity: Rarity = Rarity.COMMON) : DNDEquipmentItem(settings) {
	override val type: DNDEquipmentType = DNDEquipmentType.ARMOUR

	companion object {
		fun getStack(item: DNDEquipmentItem, value: Int, level: Int, rarityBoost: Int): ItemStack{
			val stack = ItemStack(item, 1)
			stack.set(MTTComponents.VALUE_OVERRIDE_TYPE, value)
			stack.set(MTTComponents.RARITY_OVERRIDE_TYPE, level + rarityBoost)
			stack.set(MTTComponents.ARMOUR_CLASS_BONUS_TYPE, level)
			stack.set(DataComponentTypes.ITEM_NAME, item.name.copy().append(" + $level").withColor(Rarity.getLevel(level + rarityBoost).colour))
			return stack
		}

		fun addTo(item: DNDEquipmentItem, entries: ItemGroup.Entries, uncommonValue: Int, rareValue: Int, veryRareValue: Int, rarityBoost: Int){
			val uncommonStack = getStack(item, uncommonValue, 1, rarityBoost)
			val rareStack = getStack(item, rareValue, 2, rarityBoost)
			val veryRareStack = getStack(item, veryRareValue, 3, rarityBoost)

			entries.add(item)
			entries.add(uncommonStack)
			entries.add(rareStack)
			entries.add(veryRareStack)
		}
	}
}