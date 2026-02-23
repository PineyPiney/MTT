package com.pineypiney.mtt.dnd.network

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.CharacterDetails
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import java.util.*

class ServerCharacter(override val details: CharacterDetails, uuid: UUID, override val engine: ServerDNDEngine) : Character(uuid) {

	override fun attack(target: Character) {
		super.attack(target)
		val characterCombat = engine.getCombat(this)
		val otherCombat = engine.getCombat(target)

		if (characterCombat == null) {
			if (otherCombat == null) {
				engine.startCombat(this, target)
			} else otherCombat.enterCharacter(this)
		} else if (characterCombat != otherCombat) {
			if (otherCombat == null) characterCombat.enterCharacter(target)
			else {
				characterCombat.merge(otherCombat)
			}
		}
	}
}