package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.Rarity

open class DNDAccessoryItem(settings: Settings, override val value: Int, override val weight: Float, override val type: DNDEquipmentType, override val rarity: Rarity = Rarity.COMMON) : DNDEquipmentItem(settings) {

}