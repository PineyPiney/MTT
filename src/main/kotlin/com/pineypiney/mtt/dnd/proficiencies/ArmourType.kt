package com.pineypiney.mtt.dnd.proficiencies

enum class ArmourType : EquipmentType {
	LIGHT,
	MEDIUM,
	HEAVY;

	override val id: String get() = name.lowercase()
}