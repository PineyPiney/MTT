package com.pineypiney.mtt.dnd.species

import com.pineypiney.mtt.dnd.traits.Trait

class SubSpecies(val name: String, val traits: List<Trait<*>>, val namedTraits: List<NamedTrait<*>>) {
	override fun toString(): String {
		return "$name Species"
	}
}