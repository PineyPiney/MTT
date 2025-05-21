package com.pineypiney.mtt.entity

import com.pineypiney.mtt.screen.DNDScreenHandler
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerFactory
import net.minecraft.util.collection.DefaultedList
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

class DNDInventory : Inventory, ScreenHandlerFactory {

	val equipment = DefaultedList.ofSize(EQUIPMENT_SIZE, ItemStack.EMPTY)
	val items = MutableList<ItemStack>(63){ ItemStack.EMPTY }

	override fun size(): Int {
		return EQUIPMENT_SIZE + items.size
	}

	override fun isEmpty(): Boolean {
		return equipment.all { it.item == Items.AIR || it.count == 0 } && items.all { it.item == Items.AIR || it.count == 0 }
	}

	override fun getStack(slot: Int): ItemStack {
		return if(slot < EQUIPMENT_SIZE) equipment[slot]
		else items.getOrNull(slot - EQUIPMENT_SIZE) ?: ItemStack.EMPTY
	}

	override fun setStack(slot: Int, stack: ItemStack) {
		if(slot < EQUIPMENT_SIZE) equipment[slot] = stack
		else items[slot - EQUIPMENT_SIZE] = stack
		stack.capCount(getMaxCount(stack))
		markDirty()
	}

	/**
	 * Returns if these two stacks can be added together, i.e. they are the same type,
	 * have the same data and the existing stack is not full
	 */
	private fun canStackAddMore(existingStack: ItemStack, stack: ItemStack): Boolean {
		return !existingStack.isEmpty && ItemStack.areItemsAndComponentsEqual(existingStack, stack)
				&& existingStack.isStackable
				&& existingStack.count < this.getMaxCount(existingStack)
	}

	/**
	 * Returns the index of the first empty slot, or -1 if no slots are empty
	 */
	fun getEmptySlot(): Int {
		for (i in 0..<items.size) {
			if (items[i].isEmpty) return i + EQUIPMENT_SIZE
		}
		return -1
	}

	private fun addStack(stack: ItemStack): Int {
		var i = getOccupiedSlotWithRoomForStack(stack)
		if (i == -1) i = getEmptySlot()

		return if(i == -1) stack.count else addStack(i, stack)
	}

	/**
	 * Adds [stack] to the ItemStack at [slot] and
	 * returns how many items cannot fit in the slot
	 */
	private fun addStack(slot: Int, stack: ItemStack): Int {
		var i = stack.count
		// itemStack is the itemstack already in the slot
		var itemStack: ItemStack = getStack(slot)
		// If the stack being inserted into is empty then set the item as the same item type
		if (itemStack.isEmpty) {
			itemStack = stack.copyWithCount(0)
			this.setStack(slot, itemStack)
		}

		// J is the space left in this stack
		val j = getMaxCount(itemStack) - itemStack.count
		// k is the maximum amount that can be inserted in the stack
		val k = min(i, j)
		// No new elements were inserted, so return the same number
		if (k == 0) return i

		// add k items to this slot, and remove this amount from the return value
		i -= k
		itemStack.increment(k)
		itemStack.bobbingAnimationTime = 5
		return i
	}

	private fun addStackToNewSlots(stack: ItemStack){
		if(stack.isEmpty) return
		items.add(stack.copyAndEmpty())
		items.addAll(List(8){ ItemStack.EMPTY} )
	}

	fun getOccupiedSlotWithRoomForStack(stack: ItemStack): Int {

		for (i in 0..<items.size) {
			if (this.canStackAddMore(items[i], stack)) {
				return i + 20
			}
		}

		return -1
	}

	/**
	 *  Try's to insert as much of the stack into [slot] as can fit.
	 *  If slot is -1 then it tries to fit [stack] in the whole inventory
	 */
	fun insertStack(slot: Int, stack: ItemStack): Boolean {
		if (stack.isEmpty) return false
		var i: Int
		do {
			i = stack.count
			stack.count = if (slot == -1) addStack(stack)
			else addStack(slot, stack)
		}
		while (!stack.isEmpty && stack.count < i)

		if(stack.count > 0) addStackToNewSlots(stack)
		return true
	}

	override fun removeStack(slot: Int, amount: Int): ItemStack? {
		val itemStack = if(slot < EQUIPMENT_SIZE) Inventories.splitStack(equipment, slot, amount)
		else Inventories.splitStack(items, slot - EQUIPMENT_SIZE, amount)
		if (!itemStack.isEmpty) {
			this.markDirty()
		}

		return itemStack
	}

	override fun removeStack(slot: Int): ItemStack {
		if(slot > EQUIPMENT_SIZE) {
			val itemStack: ItemStack = items.getOrNull(slot - EQUIPMENT_SIZE) ?: return ItemStack.EMPTY
			if (itemStack.isEmpty) return ItemStack.EMPTY
			items[slot - EQUIPMENT_SIZE] = ItemStack.EMPTY
			return itemStack
		}
		else {
			val itemStack: ItemStack = equipment[slot]
			if (itemStack.isEmpty) return ItemStack.EMPTY
			equipment[slot] = ItemStack.EMPTY
			return itemStack
		}
	}

	override fun markDirty() {

	}

	fun writeNbt(manager: DynamicRegistryManager, nbt: NbtList = NbtList()): NbtList{
		val equipment = NbtList()
		for(i in 0..<EQUIPMENT_SIZE){
			val item = this.equipment[i]
			if(item.isEmpty) continue
			val compound = NbtCompound()
			compound.putByte("Slot", i.toByte())
			equipment.add(item.toNbt(manager, compound))
		}

		val inventory = NbtList()
		for(i in 0..<items.size){
			val item = items[i]
			if(item.isEmpty) continue
			val compound = NbtCompound()
			compound.putInt("Slot", i)
			inventory.add(item.toNbt(manager, compound))
		}

		nbt.add(equipment)
		nbt.add(inventory)
		return nbt
	}

	fun readNbt(nbt: NbtList, manager: DynamicRegistryManager){
		val equipmentData = nbt.getListOrEmpty(0)
		if(!equipmentData.isEmpty) {
			equipment.clear()
			for (i in 0..<EQUIPMENT_SIZE) {
				val compound = equipmentData.getCompoundOrEmpty(i)
				val slot = compound.getByte("Slot").getOrNull() ?: continue
				val stack = ItemStack.fromNbt(manager, compound).orElse(ItemStack.EMPTY)
				equipment[slot.toInt()] = stack
			}
		}

		val itemsData = nbt.getListOrEmpty(1)
		if(!itemsData.isEmpty) {

			items.clear() // Inventory should have at least 63 slots
			for(j in 0..62) items.add(ItemStack.EMPTY)

			for (i in 0..<itemsData.size) {
				val compound = itemsData.getCompoundOrEmpty(i)
				val slot = compound.getInt("Slot").getOrNull() ?: continue
				val stack = ItemStack.fromNbt(manager, compound).orElse(ItemStack.EMPTY)

				// If the inventory is big enough then set the this
				if(items.size > slot) items[slot] = stack

				// Otherwise fill out the slots in-between
				else {
					while(items.size < slot) items.add(ItemStack.EMPTY)
					items.add(stack)
				}
			}
		}
	}

	override fun canPlayerUse(player: PlayerEntity?): Boolean = true

	override fun clear() {
		items.clear()
		equipment.clear()
		markDirty()
	}

	override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity?): ScreenHandler? {
		return DNDScreenHandler(syncId, playerInventory, this)
	}

	companion object {
		val EQUIPMENT_SIZE = 20
	}
}