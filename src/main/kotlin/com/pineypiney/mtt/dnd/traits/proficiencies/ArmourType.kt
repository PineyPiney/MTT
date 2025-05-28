package com.pineypiney.mtt.dnd.traits.proficiencies

enum class ArmourType : EquipmentType {
	LIGHT,
	MEDIUM,
	HEAVY;

	override val id: String get() = name.lowercase()
}