package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size

class SheetCharacter(val sheet: CharacterSheet) : Character() {
	override val type: CreatureType get() = sheet.type
	override val size: Size get() = sheet.size
	override val speed: Int get() = sheet.speed
	override val abilities: Abilities get() = sheet.abilities
}