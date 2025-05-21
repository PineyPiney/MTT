package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.MTT
import net.minecraft.util.Identifier

class DNDEquipmentType(val icon: Identifier) {

	companion object {
		val MELEE_WEAPON = DNDEquipmentType(Identifier.ofVanilla("container/slot/sword"))
		val RANGED_WEAPON = DNDEquipmentType(Identifier.of(MTT.MOD_ID, "container/slot/bow"))
		val HELMET = DNDEquipmentType(Identifier.ofVanilla("container/slot/helmet"))
		val CLOAK = DNDEquipmentType(Identifier.of(MTT.MOD_ID, "container/slot/cloak"))
		val ARMOUR = DNDEquipmentType(Identifier.ofVanilla("container/slot/chestplate"))
		val BRACERS = DNDEquipmentType(Identifier.of(MTT.MOD_ID, "container/slot/bracers"))
		val BOOTS = DNDEquipmentType(Identifier.ofVanilla("container/slot/boots"))
		val TORCH = DNDEquipmentType(Identifier.of(MTT.MOD_ID, "container/slot/torch"))
		val RING = DNDEquipmentType(Identifier.of(MTT.MOD_ID, "container/slot/ring"))
		val AMULET = DNDEquipmentType(Identifier.of(MTT.MOD_ID, "container/slot/amulet"))
		val BELT = DNDEquipmentType(Identifier.of(MTT.MOD_ID, "container/slot/belt_2"))
	}
}