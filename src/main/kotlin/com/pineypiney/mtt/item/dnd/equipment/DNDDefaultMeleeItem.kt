package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.Rarity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class DNDDefaultMeleeItem(settings: Settings, weaponType: WeaponType, weight: Float, commonValue: Int, val uncommonValue: Int, val rareValue: Int, val veryRareValue: Int) : DNDMeleeItem(settings, weaponType, commonValue, weight, Rarity.COMMON) {

	override fun getName(stack: ItemStack): Text {
		val rarity = getRarity(stack)
		val name = super.getName(stack)
		return if(rarity != Rarity.COMMON) name.copy().append(" + $rarity").withColor(rarity.colour)
		else name
	}

	fun getStack(value: Int, level: Int): ItemStack{
		val stack = ItemStack(this, 1)
		stack.set(MTTComponents.VALUE_OVERRIDE_TYPE, value)
		stack.set(MTTComponents.RARITY_OVERRIDE_TYPE, level)
		stack.set(MTTComponents.HIT_BONUS_TYPE, level)
		stack.set(MTTComponents.DAMAGE_BONUS_TYPE, level)
		return stack
	}

	fun addTo(entries: ItemGroup.Entries){
		val uncommonStack = getStack(uncommonValue, 1)
		val rareStack = getStack(rareValue, 2)
		val veryRareStack = getStack(veryRareValue, 3)

		entries.add(this)
		entries.add(uncommonStack)
		entries.add(rareStack)
		entries.add(veryRareStack)
	}
}