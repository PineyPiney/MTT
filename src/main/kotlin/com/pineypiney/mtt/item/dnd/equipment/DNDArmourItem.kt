package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Rarity
import com.pineypiney.mtt.dnd.traits.proficiencies.ArmourType
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack

open class DNDArmourItem(settings: Settings, value: Int, weight: Float, model: String, texture: String, val armourClass: Int, val armourType: ArmourType, val stealthDisadvantage: Boolean, rarity: Rarity = Rarity.COMMON) : VisibleAccessoryItem(settings, value, weight, DNDEquipmentType.ARMOUR, model, texture, rarity
) {
	override val type: DNDEquipmentType = DNDEquipmentType.ARMOUR

	override fun equip(character: Character) {
		character.baseArmourClass = armourClass
	}

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