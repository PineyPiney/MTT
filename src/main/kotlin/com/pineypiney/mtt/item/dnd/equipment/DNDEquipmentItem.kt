package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.item.dnd.DNDGameItem

abstract class DNDEquipmentItem(settings: Settings) : DNDGameItem(settings) {
	abstract val type: DNDEquipmentType

	abstract fun equip(character: Character)
}