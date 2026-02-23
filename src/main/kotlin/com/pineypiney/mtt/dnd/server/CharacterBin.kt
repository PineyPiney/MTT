package com.pineypiney.mtt.dnd.server

import com.pineypiney.mtt.dnd.network.ServerCharacter

class CharacterBin(val size: Int) {

	val characters = mutableListOf<ServerCharacter>()

	fun binCharacter(character: ServerCharacter) {
		if (characters.size == size) {
			characters.removeFirst()
		}
		characters.add(character)
	}

	fun restoreCharacter(): ServerCharacter? = characters.removeLastOrNull()
}