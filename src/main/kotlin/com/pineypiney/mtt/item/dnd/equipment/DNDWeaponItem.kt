package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.Rarity
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack

abstract class DNDWeaponItem(settings: Settings): DNDEquipmentItem(settings) {

	abstract val weaponType: WeaponType

	companion object {
		fun getStack(item: DNDWeaponItem, value: Int, level: Int): ItemStack{
			val stack = ItemStack(item, 1)
			stack.set(MTTComponents.VALUE_OVERRIDE_TYPE, value)
			stack.set(MTTComponents.RARITY_OVERRIDE_TYPE, level)
			stack.set(MTTComponents.HIT_BONUS_TYPE, level)
			stack.set(MTTComponents.DAMAGE_BONUS_TYPE, level)
			stack.set(DataComponentTypes.ITEM_NAME, item.name.copy().append(" + $level").withColor(Rarity.getLevel(level).colour))
			return stack
		}

		fun addTo(item: DNDWeaponItem, entries: ItemGroup.Entries, uncommonValue: Int, rareValue: Int, veryRareValue: Int){
			val uncommonStack = getStack(item, uncommonValue, 1)
			val rareStack = getStack(item, rareValue, 2)
			val veryRareStack = getStack(item, veryRareValue, 3)

			entries.add(item)
			entries.add(uncommonStack)
			entries.add(rareStack)
			entries.add(veryRareStack)
		}
	}
}