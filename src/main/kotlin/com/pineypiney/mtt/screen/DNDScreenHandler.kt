package com.pineypiney.mtt.screen

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.entity.DNDInventory
import com.pineypiney.mtt.item.dnd.equipment.DNDEquipmentType
import com.pineypiney.mtt.screen.slot.DNDEquipmentSlot
import com.pineypiney.mtt.screen.slot.DNDMainHandWeaponSlot
import com.pineypiney.mtt.screen.slot.DNDOffhandWeaponSlot
import com.pineypiney.mtt.screen.slot.DNDSlot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.math.MathHelper
import kotlin.math.max

class DNDScreenHandler(syncID: Int, val character: Character) : ScreenHandler(null, syncID) {

	val inventory = character.inventory

	init {
		addInventorySlots()
		addEquipmentSlots()
	}

	override fun quickMove(
		player: PlayerEntity?,
		slot: Int
	): ItemStack? {
		return ItemStack.EMPTY
	}

	override fun canUse(player: PlayerEntity?): Boolean {
		return true
	}

	override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType, player: PlayerEntity) {
		if(actionType != SlotActionType.SWAP) super.onSlotClick(slotIndex, button, actionType, player)
	}

	private fun getOverflowRows(): Int {
		return MathHelper.ceilDiv(inventory.items.size, 9) - 7
	}

	private fun getRow(scroll: Float): Int {
		return max((scroll * getOverflowRows() + 0.5).toInt(), 0)
	}

	private fun getScrollPosition(row: Int): Float {
		return MathHelper.clamp(row.toFloat() / getOverflowRows(), 0f, 1f)
	}

	fun scrollItems(position: Float) {
		val i: Int = this.getRow(position)

		for (j in 0..6) {
			for (k in 0..8) {
				val l = k + (j + i) * 9
				if (l >= 0 && l < inventory.size()) inventory.setStack(k + j * 9, inventory.items[l])
				else inventory.setStack(k + j * 9, ItemStack.EMPTY)
			}
		}
	}

	private fun addArmourSlots(i: Int, type: DNDEquipmentType){
		addSlot(DNDEquipmentSlot(type, inventory, i * 2, 8, 19 + 18 * i))
		addSlot(DNDEquipmentSlot(type, inventory, i * 2 + 1, 26, 19 + 18 * i))
	}

	private fun addEquipmentSlots(){
		addArmourSlots(0, DNDEquipmentType.HELMET)
		addArmourSlots(1, DNDEquipmentType.CLOAK)
		addArmourSlots(2, DNDEquipmentType.ARMOUR)
		addArmourSlots(3, DNDEquipmentType.BRACERS)
		addArmourSlots(4, DNDEquipmentType.BOOTS)

		addSlot(DNDMainHandWeaponSlot(DNDEquipmentType.MELEE_WEAPON, inventory, 10, 11, 86, 19))
		addSlot(DNDOffhandWeaponSlot(DNDEquipmentType.MELEE_WEAPON, inventory, 11, 10, 104, 19))
		addSlot(DNDMainHandWeaponSlot(DNDEquipmentType.RANGED_WEAPON, inventory, 12, 13, 86, 37))
		addSlot(DNDOffhandWeaponSlot(DNDEquipmentType.RANGED_WEAPON, inventory, 13, 12, 104, 37))
		addSlot(DNDEquipmentSlot(DNDEquipmentType.TORCH, inventory, 14, 86, 55))
		//addSlot(DNDEquipmentSlot(DNDEquipmentType.SHIELD, inventory, 15, 104, 55))
		addSlot(DNDEquipmentSlot(DNDEquipmentType.BELT, inventory, 16, 86, 73))
		addSlot(DNDEquipmentSlot(DNDEquipmentType.AMULET, inventory, 17, 104, 73))
		addSlot(DNDEquipmentSlot(DNDEquipmentType.RING, inventory, 18, 86, 91))
		addSlot(DNDEquipmentSlot(DNDEquipmentType.RING, inventory, 19, 104, 91))
	}

	private fun addInventorySlots(){
		for(j in 0..6){
			for(i in 0..8){
				val id = DNDInventory.EQUIPMENT_SIZE + j*9 + i
				addSlot(DNDSlot(inventory, id,  140 + i * 18, 18 + j * 18))
			}
		}
	}
}