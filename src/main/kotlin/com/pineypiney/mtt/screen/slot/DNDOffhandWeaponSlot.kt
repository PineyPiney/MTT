package com.pineypiney.mtt.screen.slot

import com.pineypiney.mtt.dnd.proficiencies.WeaponType
import com.pineypiney.mtt.item.dnd.equipment.DNDEquipmentType
import com.pineypiney.mtt.item.dnd.equipment.DNDWeaponItem
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

class DNDOffhandWeaponSlot(type: DNDEquipmentType, inventory: Inventory, slot: Int, private val mainHandSlot: Int, x: Int, y: Int) : DNDEquipmentSlot(type, inventory, slot, x, y) {

	override fun canInsert(stack: ItemStack): Boolean {
		if(!super.canInsert(stack)) return false
		val weapon = stack.item as? DNDWeaponItem
		// Can't put two-handed weapons in the offhand slot
		if(weapon?.weaponType?.twoHanded == true) return false

		// If the main hand doesn't contain a weapon then the offhand spot is free
		val mainWeapon = getMainHandWeaponType() ?: return true
		return !mainWeapon.twoHanded		// Can insert if the main weapon isn't two handed
	}

	//override fun getBackgroundSprite(): Identifier? {
	//	val mainHandType = getMainHandWeaponType() ?: return type.icon
	//	return if(mainHandType.twoHanded) mainHand.stack.get(DataComponentTypes.ITEM_MODEL)
	//	else type.icon
	//}

	private fun getMainHandWeaponType(): WeaponType? {
		val mainHandStack = inventory.getStack(mainHandSlot)
		// Not holding anything in the main hand so the offhand is free
		return if(mainHandStack.isEmpty) null
		else (mainHandStack.item as? DNDWeaponItem)?.weaponType
	}
}