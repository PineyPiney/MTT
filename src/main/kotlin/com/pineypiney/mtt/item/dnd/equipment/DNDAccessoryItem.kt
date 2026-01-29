package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.CoinValue
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Rarity

open class DNDAccessoryItem(settings: Settings, override val value: CoinValue, override val weight: Float, override val type: DNDEquipmentType, override val rarity: Rarity = Rarity.COMMON) : DNDEquipmentItem(settings) {

	override fun equip(character: Character) {

	}
}