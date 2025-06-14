package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.item.dnd.DNDItem

abstract class DNDEquipmentItem(settings: Settings) : DNDItem(settings) {
	abstract val type: DNDEquipmentType

	abstract fun equip(character: Character)
}