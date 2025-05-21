package com.pineypiney.mtt.screen.slot

import com.pineypiney.mtt.item.dnd.equipment.DNDEquipmentItem
import com.pineypiney.mtt.item.dnd.equipment.DNDEquipmentType
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

open class DNDEquipmentSlot(val type: DNDEquipmentType, inventory: Inventory, slot: Int, x: Int, y: Int) : DNDSlot(inventory, slot, x, y) {

	override fun canInsert(stack: ItemStack): Boolean {
		return (stack.item as? DNDEquipmentItem)?.type == type
	}

	override fun getBackgroundSprite(): Identifier? {
		return type.icon
	}
}