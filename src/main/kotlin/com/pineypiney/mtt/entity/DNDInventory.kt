package com.pineypiney.mtt.entity

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.item.dnd.DNDItems
import com.pineypiney.mtt.item.dnd.equipment.DNDArmourItem
import com.pineypiney.mtt.item.dnd.equipment.DNDEquipmentItem
import com.pineypiney.mtt.item.dnd.equipment.DNDWeaponItem
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.StackWithSlot
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.storage.ReadView
import net.minecraft.storage.WriteView
import net.minecraft.util.collection.DefaultedList
import kotlin.math.min

class DNDInventory(val character: Character) : Inventory {

	val equipment = DefaultedList.ofSize(EQUIPMENT_SIZE, ItemStack.EMPTY)
	val items = MutableList<ItemStack>(63){ ItemStack.EMPTY }
	var selectedSlot = 0

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

	fun getHotbarSize() = 4

	fun getHotbarSlotStack(slot: Int): ItemStack{
		return when(slot){
			0 -> getStack(10)
			1 -> getStack(12)
			2 -> getStack(14)
			3 -> SPELL_BOOK

			else -> ItemStack.EMPTY
		}
	}

	fun getHeldStack() = getHotbarSlotStack(selectedSlot)

	fun getOffhandSlotIcon(): ItemStack {
		val hand = if(selectedSlot == 1) 12 else 10
		val offhand = getStack(hand + 1)
		if(!offhand.isEmpty) return offhand

		val mainHand = getStack(hand).item
		return if (selectedSlot < 2 && (mainHand is DNDWeaponItem) && mainHand.weaponType.twoHanded) getStack(hand)
		else ItemStack.EMPTY
	}

	fun getOffhandSlotStack(): ItemStack {
		return getStack(if(selectedSlot == 1) 13 else 11)
	}

	override fun markDirty() {

	}

	fun getVisualEquipment(i: Int) = if(equipment[i + 1].isEmpty) equipment[i] else equipment[i + 1]
	fun getVisualHelmet(): ItemStack = getVisualEquipment(0)
	fun getVisualCloak(): ItemStack = getVisualEquipment(2)
	fun getVisualArmour(): ItemStack = getVisualEquipment(4)
	fun getVisualBracers(): ItemStack = getVisualEquipment(6)
	fun getVisualBoots(): ItemStack = getVisualEquipment(8)

	fun getArmour(): DNDArmourItem? = equipment[4].item as? DNDArmourItem
	fun getOffhand(): DNDEquipmentItem? = equipment[11].item as? DNDEquipmentItem

	fun readNbt(view: ReadView) {
		val equipmentData = view.getTypedListView("Equipment", StackWithSlot.CODEC)
		if(!equipmentData.isEmpty) {
			equipment.clear()
			for (stackWithSlot in equipmentData) {
				equipment[stackWithSlot.slot] = stackWithSlot.stack
			}
		}

		val itemsData = view.getTypedListView("Inventory", StackWithSlot.CODEC)
		if(!itemsData.isEmpty) {
			items.clear() // Inventory should have at least 63 slots
			repeat(63) { items.add(ItemStack.EMPTY) }

			for (stackWithSlot in itemsData) {
				val slot = stackWithSlot.slot
				val stack = stackWithSlot.stack

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

	fun writeNbt(view: WriteView) {
		val equipment = view.getListAppender("Equipment", StackWithSlot.CODEC)
		for (i in 0..<EQUIPMENT_SIZE) {
			val item = this.equipment[i]
			if (item.isEmpty) continue
			equipment.add(StackWithSlot(i, item))
		}

		val inventory = view.getListAppender("Inventory", StackWithSlot.CODEC)
		for (i in 0..<items.size) {
			val item = items[i]
			if (item.isEmpty) continue
			inventory.add(StackWithSlot(i, item))
		}
	}

	override fun canPlayerUse(player: PlayerEntity?): Boolean = true

	override fun clear() {
		items.clear()
		equipment.clear()
		markDirty()
	}

	companion object {
		val EQUIPMENT_SIZE = 20
		val SPELL_BOOK = ItemStack(DNDItems.SPELL_BOOK)
	}
}