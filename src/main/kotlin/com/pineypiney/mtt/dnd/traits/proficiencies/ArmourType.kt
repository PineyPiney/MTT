package com.pineypiney.mtt.dnd.traits.proficiencies

import kotlin.math.min

enum class ArmourType(val dexMod: (Int) -> Int) : EquipmentType {
	LIGHT({ it }),
	MEDIUM({ min(it, 2) }),
	HEAVY({ min(it, 0) });

	override val id: String get() = name.lowercase()
}