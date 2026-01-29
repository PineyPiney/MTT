package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.CoinValue
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Rarity

class DNDShieldItem(settings: Settings, override val value: CoinValue, override val weight: Float, val armourClass: Int, override val rarity: Rarity = Rarity.COMMON) : DNDEquipmentItem(settings) {
	override val type: DNDEquipmentType get() = DNDEquipmentType.MELEE_WEAPON

	override fun equip(character: Character) {

	}
}