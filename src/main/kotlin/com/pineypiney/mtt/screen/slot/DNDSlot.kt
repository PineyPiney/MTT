package com.pineypiney.mtt.screen.slot

import com.pineypiney.mtt.item.dnd.DNDItem
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

open class DNDSlot(inventory: Inventory, slot: Int, x: Int, y: Int) : Slot(inventory, slot, x, y) {

	override fun canInsert(stack: ItemStack): Boolean {
		return stack.item is DNDItem
	}
}