package com.pineypiney.mtt.dnd.server

import com.pineypiney.mtt.dnd.characters.Character

class CharacterBin(val size: Int) {

	val characters = mutableListOf<Character>()

	fun binCharacter(character: Character) {
		if (characters.size == size) {
			characters.removeFirst()
		}
		characters.add(character)
	}

	fun restoreCharacter(): Character? = characters.removeLastOrNull()
}