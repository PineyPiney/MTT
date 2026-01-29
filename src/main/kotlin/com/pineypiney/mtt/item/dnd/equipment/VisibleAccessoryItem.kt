package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.CoinValue
import com.pineypiney.mtt.dnd.traits.Rarity

open class VisibleAccessoryItem(settings: Settings, value: CoinValue, weight: Float, type: DNDEquipmentType, val model: String, val texture: String, rarity: Rarity = Rarity.COMMON) : DNDAccessoryItem(settings, value, weight, type, rarity) {

}