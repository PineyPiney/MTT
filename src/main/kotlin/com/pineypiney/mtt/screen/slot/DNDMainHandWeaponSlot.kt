package com.pineypiney.mtt.screen.slot

import com.pineypiney.mtt.item.dnd.equipment.DNDEquipmentItem
import com.pineypiney.mtt.item.dnd.equipment.DNDEquipmentType
import com.pineypiney.mtt.item.dnd.equipment.DNDWeaponItem
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

class DNDMainHandWeaponSlot(type: DNDEquipmentType, inventory: Inventory, slot: Int, private val offHandSlot: Int, x: Int, y: Int) : DNDEquipmentSlot(type, inventory, slot, x, y) {

	override fun canInsert(stack: ItemStack): Boolean {
		val weapon = stack.item as? DNDWeaponItem ?: return false
		// If the new stack is not of the correct item return false
		if(weapon.type != type) return false
		// If the offhand doesn't contain an item then anything can be put in the main hand
		if(inventory.getStack(offHandSlot).isEmpty) return true

		// Otherwise a weapon can only be put here if it is not two-handed
		return !weapon.weaponType.twoHanded
	}
}